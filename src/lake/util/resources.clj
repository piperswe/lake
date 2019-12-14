(ns lake.util.resources
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-html-compressor.core :as html-compressor]
            [clojure.java.jdbc :as j]))

(defn clj-name->resource-name
  [clj-name]
  (str/join "/" (-> clj-name name (str/replace "-" "_") (str/split #"\."))))

(defn get-resource-name
  [ns filename extension]
  (str (-> ns str clj-name->resource-name) "/" (clj-name->resource-name filename) "." (clj-name->resource-name extension)))

(defn get-resource-for-ns
  [ns name extension]
  (slurp (io/resource (get-resource-name ns name extension))))

(defmacro get-resource
  [name extension]
  `(get-resource-for-ns ~*ns* ~name ~extension))

(defmacro get-html-resource
  ([name]
   `(get-html-resource
      ~name
      {:compress-css           true
       :compress-javascript    true
       :remove-intertag-spaces true
       :remove-quotes          true}))
  ([name options]
   `(html-compressor/compress (get-resource-for-ns ~*ns* ~name :html) ~options)))

(defmacro run-query
  [db-conn query-name]
  `(j/query ~db-conn (get-resource-for-ns ~*ns* (name ~query-name) :sql)))

(defmacro execute-query!
  [db-conn query-name]
  `(j/execute! ~db-conn (get-resource-for-ns ~*ns* (name ~query-name) :sql)))

(defn query->view
  [query view-name]
  (format "create or replace view %s as %s;" (-> view-name clj-name->resource-name (str/replace "/" ".")) query))

(defmacro add-view!
  [db-conn view-name]
  `(j/execute! ~db-conn (query->view (get-resource-for-ns ~*ns* (name ~view-name) :sql) ~view-name)))