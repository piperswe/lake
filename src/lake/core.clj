(ns lake.core
  (:gen-class)
  (:require [lake.mq.server :as mq-server]
            [lake.archive.worker :as archive-worker]))

(defn modules
  []
  {:mq-server      mq-server/-main
   :archive-worker archive-worker/-main})

(defn -main
  [module-name & args]
  (clojure.pprint/pprint (apply ((keyword module-name) (modules)) args)))
