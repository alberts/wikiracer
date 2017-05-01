(ns wikiracer.impl
  (:require
   [wikiracer.util :refer [inspect]]
   [clojure.string :as str]
   [clj-http.client :as client]))


(def visited (atom #{}))
(def start-time (atom nil))
(def complete? (atom false))
(def not-nil (comp not nil?))
(def continue-searching? #(not @complete?))

(defn realize-chunks
  "Realizes lazy sequence in chunks. Elements must not evaluate to nil."
  [result n & {:keys [pred] :or {pred (constantly true)}}]
  (if (and (pred) (not-nil (first result)))
    (realize-chunks (drop n result)
                    n
                    :pred pred)))

(defn has-destination?
  "Checks the if a neighbor if the destination."
  [destination path neighbors]
  ;; TODO can also add watcher on complete? which only prints on state change.
  ;; Will have to save result in complete?, so you can print it.
  (when (and (continue-searching?) (some #(= % destination) neighbors))
    (println
     (->> path
          (cons destination)
          reverse
          (str/join " -> ")))
    (println
     (str "Time (ms): "
          (- (System/currentTimeMillis)
             @start-time)))
    (reset! complete? true)))

(defn fetch-neighbors
  "Gets the neighbors for the most recent node in a path.

  Takes care of handling pagination via recursion."
  [destination path & {:keys [neighbors plcontinue] :or {neighbors []}}]
  (let [params        {"action"  "query"
                       "prop"    "links"
                       "titles"  (first path)
                       "format"  "json"
                       "pllimit" "max"}
        params        (if plcontinue
                        (assoc params "plcontinue" plcontinue)
                        params)
        resp          (-> (client/get "http://en.wikipedia.org/w/api.php"
                                      {:accept       :json
                                       :as           :json
                                       :query-params params})
                          :body)
        plcontinue    (-> resp :continue :plcontinue)
        new-neighbors (->> resp :query :pages vals first :links (map :title))
        neighbors     (concat neighbors new-neighbors)]
    (cond
      (some #(= % destination) new-neighbors) (has-destination? destination path new-neighbors)
      plcontinue                              (fetch-neighbors
                                               destination
                                               path
                                               :plcontinue plcontinue
                                               :neighbors neighbors)
      :else                                   {:path      path
                                               :neighbors neighbors})))

(defn get-new-paths
  [path->neighbors]
  (let [{:keys [path neighbors]} path->neighbors
        new-neighbors            (remove @visited neighbors)
        _                        (swap! visited into new-neighbors)]
    (map #(cons % path) new-neighbors)))

(defn find-path
  [destination paths max-depth & {:keys [n] :or {n 1}}]
  (let [paths->neighbors (pmap #(future (fetch-neighbors destination %)) paths)
        new-paths        (mapcat #(get-new-paths @%) paths->neighbors)]
    (cond
      @complete?      :done
      (< n max-depth) (find-path destination
                                 new-paths
                                 max-depth
                                 :n (inc n))
      :else           (realize-chunks new-paths 1000 :pred continue-searching?))))

(defn search-for-path
  [source destination max-depth]
  (inspect source destination max-depth)
  (reset! complete? false)
  (reset! start-time (System/currentTimeMillis))
  (reset! visited #{})
  (find-path destination [[source]] max-depth)
  (when (continue-searching?)
    (println "Could not find link.")
    (str "Time (ms): "
         (- (System/currentTimeMillis)
            @start-time)))
  :done)


