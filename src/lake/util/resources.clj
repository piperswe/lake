(ns lake.util.resources
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-html-compressor.core :as html-compressor]))

(defn get-resource-name
  [ns filename extension]
  (str (str/join "/" (str/split (str ns) #"\.")) "/" (name filename) "." (name extension)))

(defn get-resource-for-ns
  [ns name extension]
  (slurp (io/resource (get-resource-name ns name extension))))

(defmacro get-resource
  [name extension]
  (get-resource-for-ns *ns* name extension))

(defmacro get-html-resource
  ([name]
   `(get-html-resource
      ~name
      {:compress-css           true
       :compress-javascript    true
       :remove-intertag-spaces true
       :remove-quotes          true}))
  ([name options]
   (html-compressor/compress (get-resource-for-ns *ns* name :html) options)))