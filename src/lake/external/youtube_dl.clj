(ns lake.external.youtube-dl
  (:require [clojure.java.shell :as shell]
            [manifold.deferred :as d]))

(defn run
  [options flags url]
  (d/future
    (apply shell/sh
           (concat
             ["youtube-dl"]
             (map #(vector (format "--%s" (-> % first name)) (second %)) options)
             (map #(format "--%s" (name %)) flags)
             [url]))))
