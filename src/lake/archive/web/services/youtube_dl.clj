(ns lake.archive.web.services.youtube-dl
  (:require [lake.external.youtube-dl :as youtube-dl]
            [byte-streams :as bs]
            [clojure.java.io :as io]
            [cheshire.core :as c]
            [lake.archive.web.core :as web])
  (:import (java.io File)))

(defn archive
  [{:keys [enqueue]} url]
  (let [output-file (File/createTempFile "lake-youtube-dl" ".mkv")]
    (try
      (let [options {:output       (.getPath output-file)
                     :user-agent   "https://github.com/piperswe/lake via youtube-dl"
                     :recode-video "mkv"
                     :format       "best"}
            flags #{:print-json
                    :embed-subs
                    :embed-thumbnail
                    :add-metadata}
            {:keys [exit out err]} (youtube-dl/run options flags url)]
        (enqueue :save
                 (bs/to-byte-array (io/reader output-file))
                 (web/url->path url)
                 {:exit exit
                  :out  (try
                          (c/decode out)
                          (catch Exception e
                            out))
                  :err  err}))
      (finally
        (.delete output-file)))))
