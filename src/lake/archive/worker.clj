(ns lake.archive.worker
  (:gen-class)
  (:require [lake.mq.client :as mq]
            [lake.archive.web.core :as web]
            [lake.util.environment :as env]))

(defn archive
  [client url]
  (web/archive client url))

(defn -main
  ([]
   (-main "ws://lake-mq/"))
  ([mq-url]
   (mq/consume mq-url :archive ::-main archive)))
