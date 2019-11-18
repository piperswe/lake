(ns lake.archive.web.css
  (:require [byte-streams :as bs]
            [cheshire.core :as json]
            [clojurewerkz.urly.core :as urly]))

(def import-regex #"@import (url\()?(\".+\")(\))?;")

(defn ->ref
  [url [_ _ url-literal]]
  (urly/resolve url (json/parse-string url-literal)))

(defn archive
  [url res]
  {:body (:body res)
   :refs (map (partial ->ref url) (re-seq import-regex (bs/to-string (:body res))))})
