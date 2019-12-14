(ns lake.mq.client
  (:require [lake.rpc.client :as rpc]
            [manifold.deferred :as d]))

(defn create-client
  [url]
  (rpc/create-fn-client url))

(defn consume
  [url channel worker-name callback]
  (let [client (create-client url)
        {:keys [enqueue dequeue]} client]
    (loop [msg @(dequeue channel)]
      (try
        @(enqueue :worker-result
                  channel
                  worker-name
                  (apply (partial callback client) msg))
        (catch Exception e
          (.printStackTrace e)
          @(enqueue :worker-errors channel worker-name (.toString e))))
      (recur @(dequeue channel)))))
