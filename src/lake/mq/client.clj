(ns lake.mq.client
  (:require [lake.rpc.client :as rpc]
            [manifold.deferred :as d]))

(defn create-client
  [url]
  (rpc/create-fn-client url))

