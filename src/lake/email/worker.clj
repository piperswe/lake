(ns lake.email.worker
  (:require [clojure.java.jdbc :as j]
            [lake.db.core :as db]
            [lake.mq.client :as mq]
            [cheshire.core :as c]))

(defn part->db
  [message part]
  {:message     message
   :contentType (:content-type part)
   :body        (:body part)})

(defn format-email
  [email]
  (cond
    (not (map? email))
    ""

    (and (:address email) (:name email))
    (format "%s <%s>" (:name email) (:address email))

    (:address email)
    (:address email)

    (:name email)
    (:name email)

    :else
    ""))

(defn vec->db
  [db-conn vec]
  (let [conn (j/get-connection db-conn)]
    (.createArrayOf conn "text" (into-array String vec))))

(defn message->db
  [message db-conn]
  {:subject     (:subject message)
   :emailFrom   (format-email (:from message))
   :received    (:date-received message)
   :toList      (->> message :to (map format-email) (vec->db db-conn))
   :cc          (->> message :cc (map format-email) (vec->db db-conn))
   :bcc         (->> message :bcc (map format-email) (vec->db db-conn))
   :multipart   (:multipart? message)
   :contentType (:content-type message)
   :sender      (format-email (:sender message))
   :sent        (:date-sent message)
   :headers     (apply merge (:headers message))})

(defn add-message
  [db-conn _ message]
  (let [db-message (message->db message db-conn)
        res (j/insert! db-conn
                       :email.message
                       db-message
                       {:return-keys ["id"]})]
    (j/insert-multi! db-conn
                     :email.part
                     (map (partial part->db (-> res first :id)) (:body message)))))

(defn -main
  ([]
   (-main "ws://lake-mq/"))
  ([mq-url]
   (j/with-db-connection
     [db-conn (db/config)]
     (-main mq-url db-conn)))
  ([mq-url db-conn]
   (mq/consume mq-url :email ::-main (partial add-message db-conn))))
