(ns lake.util.environment)

(defn get
  [name default]
  (or (System/getenv name) default))
