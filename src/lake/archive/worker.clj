(ns lake.archive.worker
  (:gen-class)
  (:require [lake.mq.client :as mq]
            [lake.archive.web.core :as web]))

(defn archive
  [client url]
  (web/archive client url))

(defn -main
  [mq-url]
  (mq/consume mq-url :archive archive))
