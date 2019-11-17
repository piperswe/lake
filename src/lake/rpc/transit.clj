(ns lake.rpc.transit
  (:require [cognitect.transit :as t])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)))

(defn read
  [data]
  (let [in (ByteArrayInputStream. data)
        reader (t/reader in :json-verbose)]
    (t/read reader)))

(defn write
  [data]
  (let [out (ByteArrayOutputStream.)
        writer (t/writer out :json-verbose)]
    (t/write writer data)
    (.toByteArray out)))