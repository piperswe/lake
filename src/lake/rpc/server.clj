(ns lake.rpc.server
  (:require [aleph.http :as http]
            [lake.util.resources :as r]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [lake.rpc.transit :as t]))

(defn handle-command
  [handlers command]
  (let [command-name (first command)]
    (if (nil? command-name)
      {:status :error
       :error  :must-specify-command}
      (let [command-fn (command-name handlers)]
        (if (nil? command-fn)
          {:status :error
           :error  :no-such-command}
          (-> (apply command-fn (rest command))
              (d/chain #(assoc {:status :success} :result %))
              (d/catch Exception #(do
                                    (.printStackTrace %)
                                    {:status :error
                                     :error  %}))))))))

(defn handle-command-data
  [handlers socket command]
  (d/let-flow [full-parsed-command (t/read command)
               id (:id full-parsed-command)
               parsed-command (:command full-parsed-command)
               result (handle-command handlers parsed-command)
               serialized-result (t/write (assoc result :id id))]
    (s/put! socket serialized-result)))

(def invalid-request
  {:status       400
   :content-type "text/html"
   :body         (r/get-html-resource :invalid-request)})

(defn create-server
  [handlers]
  (http/start-server
    (fn
      [req]
      (-> (http/websocket-connection req)
          (d/chain
            (fn
              [socket]
              (s/consume (partial handle-command-data handlers socket) socket)))
          (d/catch
            (fn
              [_]
              invalid-request))))
    {:port 8080}))
