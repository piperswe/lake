(ns lake.util.environment)

(def development-env (atom {}))

(defn get
  [name default]
  (or (@development-env name) (System/getenv name) default))

(defn set
  [name value]
  (swap! development-env assoc name value))