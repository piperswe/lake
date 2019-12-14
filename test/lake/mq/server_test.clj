(ns lake.mq.server-test
  (:require [clojure.test :refer :all]
            [lake.mq.server :refer :all]
            [mockery.core :as m]
            [manifold.stream :as s]
            [lake.rpc.transit :as t]
            [byte-streams :as bs])
  (:import (java.sql Timestamp)))

(deftest add-to-db-queue-test
  (testing "Inserts items into vector"
    (let [db-queue (atom [])]
      (add-to-db-queue db-queue "test item")
      (add-to-db-queue db-queue "second test item")
      (add-to-db-queue db-queue {:third-test "item"})
      (is (= @db-queue ["test item"
                        "second test item"
                        {:third-test "item"}])))))

(deftest flush-db-queue-test
  (testing "Writes items to database"
    (let [message {:timestamp (Timestamp. 0)
                   :channel   "a-channel"
                   :arguments "[]"}]
      (m/with-mock mock
        {:target :clojure.java.jdbc/insert-multi!
         :return nil}
        (flush-db-queue (atom [message]) nil)
        (is (= (:call-args-list @mock) `[(nil :messages [~message])]))))))

(deftest get-channel-stream-test
  (testing "Gets existing stream"
    (let [mq (atom {:channel       "stream"
                    :other-channel "other stream"})]
      (is (= (get-channel-stream mq :channel) "stream"))
      (is (= (get-channel-stream mq :other-channel) "other stream"))))
  (testing "Creates new streams"
    (let [mq (atom {})]
      (is (get-channel-stream mq :channel))
      (is (= (get-channel-stream mq :channel) (get-channel-stream mq :channel)))
      (s/put! (get-channel-stream mq :channel) "message")
      (is (= @(s/take! (get-channel-stream mq :channel)) "message"))

      (is (get-channel-stream mq :other-channel))
      (is (= (get-channel-stream mq :other-channel) (get-channel-stream mq :other-channel)))
      (is (not= (get-channel-stream mq :channel) (get-channel-stream mq :other-channel)))
      (s/put! (get-channel-stream mq :channel) "message")
      (s/put! (get-channel-stream mq :other-channel) "other message")
      (is (= @(s/take! (get-channel-stream mq :other-channel)) "other message"))
      (is (= @(s/take! (get-channel-stream mq :channel)) "message")))))

(deftest enqueue-command-test
  (testing "Adds commands to the DB queue and stream"
    (let [stream (s/stream)]
      (m/with-mocks
        [_ {:target :lake.db.core/now
            :return 0}
         add {:target :lake.mq.server/add-to-db-queue}
         get-stream {:target :lake.mq.server/get-channel-stream
                     :return stream}]
        (let [command (enqueue-command :mq :db-queue)
              first-message {:timestamp (Timestamp. 0)
                             :channel   :channel
                             :arguments '(:arg1 :arg2)}
              second-message {:timestamp (Timestamp. 0)
                              :channel   :second-channel
                              :arguments '(:an-arg [:another #{:arg}])}
              expected-first-result {:timestamp 0
                                     :channel   (name (:channel first-message))
                                     :arguments (-> first-message :arguments t/write (bs/convert String))}
              expected-second-result {:timestamp 0
                                      :channel   (name (:channel second-message))
                                      :arguments (-> second-message :arguments t/write (bs/convert String))}
              first-result (apply command (conj (:arguments first-message) (:channel first-message)))
              second-result (apply command (conj (:arguments second-message) (:channel second-message)))]
          (is (= first-result expected-first-result))
          (is (= second-result expected-second-result))
          (is (= @(s/take! stream) (:arguments first-message)))
          (is (= @(s/take! stream) (:arguments second-message)))
          (is (= (:call-args-list @add) `[(:db-queue ~expected-first-result) (:db-queue ~expected-second-result)]))
          (is (= (:call-args-list @get-stream) '[(:mq :channel) (:mq :second-channel)])))))))

(deftest dequeue-command-test
  (let [stream (s/stream)]
    (m/with-mock mock
      {:target :lake.mq.server/get-channel-stream
       :return stream}
      (let [command (dequeue-command :mq)
            first-message '(:arg1 :arg2)
            second-message '(:arg3 :arg4)]
        (testing "Gets existing messages from the queue"
          (s/put! stream first-message)
          (s/put! stream second-message)
          (is (= @(command :channel) first-message))
          (is (:call-args @mock) '(:mq :channel))
          (is (:call-count @mock) 1)
          (is (= @(command :channel) second-message))
          (is (:call-args @mock) '(:mq :channel))
          (is (:call-count @mock) 2))
        (testing "Gets future messages from the queue"
          (let [first-result (command :channel)
                _ (is (:call-args @mock) '(:mq :channel))
                _ (is (:call-count @mock) 3)
                second-result (command :channel)
                _ (is (:call-args @mock) '(:mq :channel))
                _ (is (:call-count @mock) 4)]
            (s/put! stream first-message)
            (s/put! stream second-message)
            (is (= @first-result first-message))
            (is (= @second-result second-message))))))))
