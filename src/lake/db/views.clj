(ns lake.db.views
  (:require [clojure.java.jdbc :as j]
            [lake.util.resources :as r]))

(def views
  [:email.message-text
   :mq.worker-error
   :mq.queue])

(defn create-views
  [db-conn]
  (j/with-db-transaction [tx-conn db-conn]
    (doseq [view views]
      (r/add-view! tx-conn view))))