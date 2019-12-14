(ns lake.core
  (:gen-class)
  (:require [lake.mq.server :as mq-server]
            [lake.archive.worker :as archive-worker]
            [lake.web.server :as web-server]
            [lake.email.worker :as email-worker]))

(defn modules
  []
  {:mq-server      mq-server/-main
   :archive-worker archive-worker/-main
   :web-server     web-server/-main
   :email-worker   email-worker/-main})

(defn -main
  [module-name & args]
  (clojure.pprint/pprint (apply ((keyword module-name) (modules)) args)))
