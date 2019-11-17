(ns lake.rpc.client
  (:require [manifold.deferred :as d]
            [aleph.http :as http]
            [manifold.stream :as s]
            [lake.rpc.transit :as t]
            [potemkin :as p])
  (:import (java.util UUID)))

(defn add-outstanding-request
  [outstanding-requests id deferred]
  (assoc outstanding-requests id deferred))

(defn fulfill-outstanding-request
  [outstanding-requests id]
  (dissoc outstanding-requests id))

(defn create-client
  [url]
  (d/let-flow [outstanding-requests (atom {})
               conn (http/websocket-client url)]
    (s/consume (fn
                 [response-data]
                 (let [response (t/read response-data)
                       id (:id response)
                       outstanding-request (@outstanding-requests id)]
                   (swap! outstanding-requests fulfill-outstanding-request id)
                   (case (:status response)
                     :success
                     (d/success! outstanding-request (:result response))
                     :error
                     (d/error! outstanding-request (Exception. (:error response)))
                     (d/error! outstanding-request (Exception. "Invalid response status")))))
               conn)
    {:outstanding-requests outstanding-requests
     :conn                 conn}))

(defn get-fn
  [client-f name]
  (fn
    [& args]
    (d/let-flow [client client-f
                 conn (:conn client)
                 outstanding-requests (:outstanding-requests client)
                 id (.toString (UUID/randomUUID))
                 _ (s/put! conn (t/write {:id      id
                                          :command (concat (conj args name))}))
                 result {:deferred (d/deferred)}
                 _ (swap! outstanding-requests add-outstanding-request id (:deferred result))
                 true-result (:deferred result)]
      true-result)))

(p/def-map-type FnClient [client mta]
  (get [_ k _]
    (get-fn client k))
  (assoc [x _ _]
    x)
  (dissoc [x _]
    x)
  (keys [_]
    [])
  (meta [_]
    mta)
  (with-meta [_ mta]
    (FnClient. client mta)))

(defn create-fn-client
  [url]
  (let [client (create-client url)]
    (FnClient. client {})))