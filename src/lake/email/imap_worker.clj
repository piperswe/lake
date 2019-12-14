(ns lake.email.imap-worker
  (:require [clojure-mail.core :refer :all]
            [clojure-mail.message :refer [read-message]]
            [lake.mq.client :as mq]))

(defn -main
  [store]
  (let [{:keys [enqueue]} (mq/create-client "ws://localhost:8080/")]
    (doseq [message (map read-message (take 10 (all-messages store "inbox")))]
      @(enqueue :email message))))