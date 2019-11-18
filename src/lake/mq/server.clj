(ns lake.mq.server
  (:gen-class)
  (:require [lake.rpc.server :as rpc]
            [lake.rpc.transit :as t]
            [lake.db :as db]
            [clojure.java.jdbc :as j]
            [overtone.at-at :as at]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [byte-streams :as bs]))

(defn add-to-db-queue
  [db-queue insert]
  (swap! db-queue conj insert))

(defn flush-db-queue
  [db-queue db-conn]
  (let [[old-db-queue] (swap-vals! db-queue (fn [_] []))]
    (j/insert-multi! db-conn
                     :messages
                     old-db-queue)))

(defn get-channel-stream
  [mq channel]
  ((swap! mq #(assoc % channel (or (% channel) (s/stream)))) channel))

(defn enqueue-command
  [mq db-queue]
  (fn
    [channel & arguments]
    (let [message {:timestamp (db/now)
                   :channel   (name channel)
                   :arguments (bs/convert (t/write arguments) String)}
          _ (add-to-db-queue db-queue message)
          stream (get-channel-stream mq channel)]
      (s/put! stream arguments)
      message)))

(defn dequeue-command
  [mq]
  (fn
    [channel]
    (d/let-flow [stream (get-channel-stream mq channel)
                 result (s/take! stream)]
      result)))

(defn -main
  ([]
   (j/with-db-connection
     [db-conn (db/config)]
     (-main db-conn)))
  ([db-conn]
   (let [mq (atom {})
         db-queue (atom [])
         at-at-pool (at/mk-pool)]
     (at/every 5000 (partial flush-db-queue db-queue db-conn) at-at-pool)
     (rpc/create-server {:enqueue (enqueue-command mq db-queue)
                         :dequeue (dequeue-command mq)}))))
