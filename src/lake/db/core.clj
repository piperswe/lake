(ns lake.db.core
  (:require [lake.util.environment :as env]
            [clojure.java.jdbc :as j]
            [cheshire.core :as c])
  (:import (java.sql Timestamp PreparedStatement Array)
           (clojure.lang IPersistentMap IPersistentVector)
           (org.postgresql.util PGobject PGTimestamp)
           (java.util Date Calendar)))

(defn config
  []
  {:dbtype                (env/get "DB_TYPE" "postgresql")
   :dbname                (env/get "DB_NAME" "lake")
   :host                  (env/get "DB_HOST" "localhost")
   :user                  (env/get "DB_USER" "lake")
   :password              (env/get "DB_PASSWORD" "lake")
   :reWriteBatchedInserts true})

(defn now
  []
  (Timestamp. (System/currentTimeMillis)))

(extend-protocol j/ISQLValue
  IPersistentMap
  (sql-value [^IPersistentMap value]
    (doto (PGobject.)
      (.setType "json")
      (.setValue (c/generate-string value)))))

(extend-protocol j/ISQLValue
  Date
  (sql-value [^Date value]
    (PGTimestamp. (.getTime value) (Calendar/getInstance))))

(extend-protocol j/ISQLValue
  Timestamp
  (sql-value [^Timestamp value]
    (PGTimestamp. (.getTime value) (Calendar/getInstance))))

(extend-protocol j/IResultSetReadColumn
  PGobject
  (result-set-read-column [^PGobject pgobj _ _]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (c/parse-string value true)
        :else value))))
