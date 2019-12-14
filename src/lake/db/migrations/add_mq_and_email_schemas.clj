(ns lake.db.migrations.add-mq-and-email-schemas
  (:require [lake.util.resources :as r]))

(defn up
  [db-conn]
  (r/execute-query! db-conn :up))

(defn down
  [db-conn]
  (r/execute-query! db-conn :down))

(def migration
  {:description "Add mq and email schemas"
   :up up
   :down down})