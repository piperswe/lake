(ns lake.archive.web.services.core
  (:require [lake.archive.web.services.youtube-dl :as youtube-dl]))

(def services
  {"youtube.com" youtube-dl/archive})
