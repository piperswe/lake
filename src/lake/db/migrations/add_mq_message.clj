(ns lake.db.migrations.add-mq-message
  (:require [lake.util.resources :as r]))

(defn up
  [db-conn]
  (r/execute-query! db-conn :up))

(defn down
  [db-conn]
  (r/execute-query! db-conn :down))

(def migration
  {:description "Add mq.message"
   :up up
   :down down})