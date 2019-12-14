(ns lake.archive.web.core
  (:require [lake.archive.web.html :as html]
            [lake.archive.web.css :as css]
            [manifold.deferred :as d]
            [aleph.http :as http]
            [pantomime.media :as mt]
            [byte-streams :as bs]
            [clojurewerkz.urly.core :as urly]
            [clojure.string :as str]))

(defn url->path
  [url]
  (let [host (urly/host-of url)
        path (urly/path-of url)]
    (if (str/ends-with? path "/")
      (str host path "index.html")
      (str host path))))

(defn archive
  [{:keys [enqueue] :as client} url]
  (d/let-flow [res (http/get url {:headers {"User-Agent" "https://github.com/piperswe/lake"}})
               ba-res (assoc res :body (bs/to-byte-array (:body res)))
               content-type (mt/base-type ((:headers ba-res) "content-type"))
               archive-result (case (str content-type)
                                "text/html"
                                (html/archive url ba-res)
                                "text/css"
                                (css/archive url ba-res)
                                {:body (:body ba-res)
                                 :refs []})]
    (d/zip (conj
             (map #(enqueue :archive %) (distinct (:refs archive-result)))
             (enqueue :save (bs/to-byte-array (:body archive-result)) (url->path url) {:status  (:status res)
                                                                                       :headers (:headers res)})))))
