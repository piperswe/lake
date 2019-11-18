(ns lake.db
  (:import (java.sql Timestamp)))

(defn from-env
  [name default]
  (or (System/getenv name) default))

(def config (memoize
              (fn
                []
                {:dbtype                (from-env "DB_TYPE" "postgresql")
                 :dbname                (from-env "DB_NAME" "lake")
                 :host                  (from-env "DB_HOST" "localhost")
                 :user                  (from-env "DB_USER" "lake")
                 :password              (from-env "DB_PASSWORD" "lake")
                 :reWriteBatchedInserts true})))

(defn now
  []
  (Timestamp. (System/currentTimeMillis)))