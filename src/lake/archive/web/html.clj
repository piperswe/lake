(ns lake.archive.web.html
  (:require [byte-streams :as bs]
            [hickory.core :as h]
            [hickory.select :as s]
            [clojurewerkz.urly.core :as urly]))

(def useful-link-refs #{"icon" "license" "copyright" "manifest" "preload" "stylesheet"})

(defn archive
  [url res]
  (let [parsed-body (-> res :body bs/to-string h/parse h/as-hickory)
        link-els (s/select (s/child (s/tag :link)) parsed-body)
        useful-link-els (filter #(contains? useful-link-refs (-> % :attrs :rel)) link-els)
        link-hrefs (remove nil? (map #(-> % :attrs :href) useful-link-els))
        script-els (s/select (s/child (s/tag :script)) parsed-body)
        script-srcs (remove nil? (map #(-> % :attrs :src) script-els))]
    {:body (:body res)
     :refs (map #(urly/resolve url %) (concat link-hrefs script-srcs))}))
