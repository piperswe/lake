(ns lake.web.server
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes app
  (GET "/" [] "Test")
  (route/not-found "Not Found"))

(defn -main
  ([]
   (-main 3000))
  ([port]
   (run-jetty app {:port port})))
