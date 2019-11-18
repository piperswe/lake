(ns lake.mq.client
  (:require [lake.rpc.client :as rpc]
            [manifold.deferred :as d]))

(defn create-client
  [url]
  (rpc/create-fn-client url))

(defn consume
  [url channel callback]
  (let [client (create-client url)
        {:keys [dequeue]} client]
    (d/loop []
      (d/chain (dequeue channel)
               (fn
                 [msg]
                 (apply (partial callback client) msg)))
      (fn
        [_]
        (d/recur)))))
