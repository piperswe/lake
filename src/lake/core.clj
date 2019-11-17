(ns lake.core
  (:gen-class)
  (:require [lake.mq.server :as mq-server]))

(defn modules
  []
  {:mq-server mq-server/-main})

(defn -main
  [module-name & args]
  (clojure.pprint/pprint (apply ((keyword module-name) (modules)) args)))
