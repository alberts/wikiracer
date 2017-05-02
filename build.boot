(def project 'wikiracer)
(def version "0.0.0")

(set-env! :resource-paths #{"resources"}
          :source-paths   #{"src"}
          :dependencies   '[[environ"1.0.3"]
                            [boot-environ "1.0.3"]
                            [cheshire "5.7.1"]
                            [clj-http "2.3.0"]
                            [org.clojure/clojure "1.8.0"]])

(task-options!
 aot {:namespace #{'wikiracer.core}}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/kenbier/wikiracer"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main 'wikiracer.core
      :file (str "wikiracer-standalone.jar")})


(deftask dev
  "Run a restartable system in the Repl"
  []
  (comp
   (watch :verbose true)
   (speak)
   (repl :server true)))

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[wikiracer.core :as app])
  (apply (resolve 'app/-main) args))

