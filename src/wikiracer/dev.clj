(ns wikiracer.dev
  (:require
   [wikiracer.impl :as impl]
   [clojure.string :as str]
   [clj-http.client :as client]))

(comment
  (impl/search-for-path "2017 London Marathon" "Washington, D.C." 6)
  (impl/search-for-path "Grand National Trial" "Mike Tyson" 6))






