(ns wikiracer.util)

(defn- inspect-1 [expr]
  `(let [result# ~expr]
     (println)
     (print (str (pr-str '~expr) " => "))
     (clojure.pprint/pprint result#)
     result#))

(defmacro inspect [& exprs]
  `(do ~@(map inspect-1 exprs)))

