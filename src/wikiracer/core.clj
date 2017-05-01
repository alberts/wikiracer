(ns wikiracer.core
  (:gen-class)
  (:require [wikiracer.impl :refer [search-for-path]]))

(def default-depth 3)

(defn parse-inputs
  [source destination depth]
  (let [depth (if (empty? depth)
                default-depth
                (try (Integer/parseInt depth)
                     (catch Exception e default-depth)))
        depth (min depth 6)]
    (if (or (.contains source "|") (.contains destination "|"))
      (throw (ex-info "Source and destination cannot have pipes."
                      {:source      source
                       :destination destination}))
      {:source      source
       :destination destination
       :depth       depth})))

(defn user-input
  []
  (println "Time to wikirace!")
  (loop []
    (let [_               (println "Enter source article title:")
          source          (read-line)
          _               (println "Enter destination title:")
          destination     (read-line)
          _               (println "Enter depth, or press enter for default. Max of 6:")
          depth           (read-line)
          {:keys [source
                  destination
                  depth]} (parse-inputs source destination depth)
          _               (search-for-path source destination depth)])
    (recur)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (user-input))
