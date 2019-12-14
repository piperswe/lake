(ns lake.user
  (:require [lake.db.migrate :as migrate]
            [clojure.java.jdbc :as j]
            [lake.db.core :as db]
            [lake.db.views :as views]
            [clojure.tools.namespace.repl :refer :all]
            [clojure-mail.core :as mail]))

(defn migrate-up
  []
  (j/with-db-connection [db-conn (db/config)]
    (migrate/up db-conn)))

(defn create-views
  []
  (j/with-db-connection [db-conn (db/config)]
    (views/create-views db-conn)))