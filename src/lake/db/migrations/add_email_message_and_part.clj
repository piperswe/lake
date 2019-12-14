(ns lake.db.migrations.add-email-message-and-part
  (:require [lake.util.resources :as r]))

(defn up
  [db-conn]
  (r/execute-query! db-conn :up))

(defn down
  [db-conn]
  (r/execute-query! db-conn :down))

(def migration
  {:description "Add email.message and email.part"
   :up up
   :down down})