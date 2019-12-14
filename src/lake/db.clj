(ns lake.db
  (:require [lake.util.environment :as env])
  (:import (java.sql Timestamp)))

(def config (memoize
              (fn
                []
                {:dbtype                (env/get "DB_TYPE" "postgresql")
                 :dbname                (env/get "DB_NAME" "lake")
                 :host                  (env/get "DB_HOST" "localhost")
                 :user                  (env/get "DB_USER" "lake")
                 :password              (env/get "DB_PASSWORD" "lake")
                 :reWriteBatchedInserts true})))

(defn now
  []
  (Timestamp. (System/currentTimeMillis)))
