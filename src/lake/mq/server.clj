(ns lake.mq.server
  (:gen-class)
  (:require [lake.rpc.server :as rpc]
            [lake.rpc.transit :as t]
            [lake.db.core :as db]
            [clojure.java.jdbc :as j]
            [overtone.at-at :as at]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [byte-streams :as bs]
            [cheshire.core :as c]))

(def beacons #{:worker-errors :worker-result})

(defn add-to-db-queue
  [db-queue insert]
  (swap! db-queue conj insert))

(defn flush-db-queue
  [db-queue]
  (try
    (j/with-db-connection
      [db-conn (db/config)]
      (let [[old-db-queue] (swap-vals! db-queue (fn [_] []))]
        (j/insert-multi! db-conn
                         :mq.message
                         old-db-queue)))
    (catch Exception e (.printStackTrace e))))

(defn get-channel-stream
  [mq channel]
  ((swap! mq #(assoc % channel (or (% channel) (s/stream 1024)))) channel))

(defn enqueue-command
  [mq db-queue]
  (fn
    [channel & arguments]
    (println (format "enqueue [%s] %d" channel (count arguments)))
    (d/let-flow [message {:timestamp (db/now)
                          :channel   (name channel)
                          :arguments (c/parse-string (bs/convert (t/write arguments) String))}
                 _ (add-to-db-queue db-queue message)
                 _ (when (not (contains? beacons channel))
                     (s/put! (get-channel-stream mq channel) arguments))]
      message)))

(defn dequeue-command
  [mq]
  (fn
    [channel]
    (d/let-flow [stream (get-channel-stream mq channel)
                 result (s/take! stream)]
      (println (format "dequeue [%s] %d" channel (count result)))
      result)))

(defn -main
  []
  (let [mq (atom {})
        db-queue (atom [])
        at-at-pool (at/mk-pool)]
    (at/every 5000 (partial flush-db-queue db-queue) at-at-pool)
    (rpc/create-server {:enqueue (enqueue-command mq db-queue)
                        :dequeue (dequeue-command mq)})))
