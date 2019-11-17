(ns lake.mq.server
  (:gen-class)
  (:import (java.sql Timestamp))
  (:require [lake.rpc.server :as rpc]
            [lake.rpc.transit :as t]
            [lake.db :as db]
            [clojure.java.jdbc :as j]
            [overtone.at-at :as at]
            [manifold.stream :as s]
            [manifold.deferred :as d]))

(defn add-to-db-queue
  [db-queue insert]
  (swap! db-queue conj insert))

(defn flush-db-queue
  [db-queue]
  (let [[old-db-queue] (swap-vals! db-queue (fn [_] []))]
    (j/insert-multi! (db/config)
                     :messages
                     old-db-queue)))

(defn get-channel-stream
  [mq channel]
  ((swap! mq #(assoc % channel (or (% channel) (s/stream)))) channel))

(defn push-command
  [mq db-queue]
  (fn
    [channel & arguments]
    (let [message {:timestamp (Timestamp. (System/currentTimeMillis))
                   :channel   (name channel)
                   :arguments (apply str (map char (t/write arguments)))}
          _ (add-to-db-queue db-queue message)
          stream (get-channel-stream mq channel)]
      (s/put! stream arguments)
      message)))

(defn pop-command
  [mq]
  (fn
    [channel]
    (d/let-flow [stream (get-channel-stream mq channel)
                 result (s/take! stream)]
      result)))

(defn -main
  []
  (let [mq (atom {})
        db-queue (atom [])
        at-at-pool (at/mk-pool)]
    (at/every 5000 (partial flush-db-queue db-queue) at-at-pool)
    (rpc/create-server {:push (push-command mq db-queue)
                        :pop  (pop-command mq)})))