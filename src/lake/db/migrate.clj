(ns lake.db.migrate
  (:require [clojure.java.jdbc :as j]
            [lake.util.resources :as r]

            [lake.db.migrations.add-mq-and-email-schemas :as add-mq-and-email-schemas]
            [lake.db.migrations.add-mq-message :as add-mq-message]
            [lake.db.migrations.add-email-message-and-part :as add-email-message-and-part]))

(def migrations
  [add-mq-and-email-schemas/migration
   add-mq-message/migration
   add-email-message-and-part/migration])

(defn current-index
  [db-conn]
  (if (empty? (r/run-query db-conn :has-migration-0))
    -1
    (let [result (r/run-query db-conn :current-index)]
      (if (empty? result)
        0
        (-> result first :idx)))))

(defn run-migration-0
  [db-conn]
  (r/execute-query! db-conn :migration-0-up))

(defn get-migrations-to-run
  [index]
  (if (= index -1)
    (concat [{:up run-migration-0}] migrations)
    (drop index migrations)))

(defn run-migration
  [db-conn direction migration]
  ((direction migration) db-conn))

(defn up
  [db-conn]
  (j/with-db-transaction [tx-conn db-conn]
    (let [index (current-index tx-conn)
          migrations-to-run (get-migrations-to-run index)]
      (doseq [migration migrations-to-run]
        (println (:description migration))
        (run-migration tx-conn :up migration))
      (j/insert-multi! tx-conn
                       :migrations.migration
                       (map-indexed (fn [idx _] {:idx (+ index idx)}) migrations-to-run)))))