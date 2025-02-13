(ns tasks.test
  (:refer-clojure :exclude [test])
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [tasks.build :refer [build install]]
            [tasks.tools :as t]))

(defn cljs* [deps main]
  (let [version (get-in deps ['org.clojure/clojurescript :mvn/version])
        out     (str "target/" (name main) "." version ".js")]
    (when (seq
           (fs/modified-since
            out
            (concat
             (fs/glob "src" "**")
             (fs/glob "test" "**"))))
      (t/clj "-Sdeps" (pr-str {:deps deps})
             "-M:test"
             "-m" :cljs.main
             "--output-dir" (str "target/cljs-output-" version)
             "--target" :node
             "--output-to" out
             "--compile" main))
    (t/node out)))

(defn cljs-runtime [version]
  (install)
  (build)
  (cljs* {'org.clojure/clojurescript {:mvn/version version}} :portal.test-runtime-runner))

(defn- get-cljs-deps []
  (get-in (read-string (slurp "deps.edn")) [:aliases :cljs :extra-deps]))

(defn cljs-ui []
  (install)
  (cljs* (get-cljs-deps) :portal.test-ui-runner))

(defn cljs []
  (cljs-runtime "1.10.773")
  (cljs-runtime "1.10.844")
  (cljs-ui))

(defn clj
  []
  (build)
  (t/clj "-M:test" "-m" :portal.test-runner)
  (t/bb "-m" :portal.test-runner))

(defn cljr []
  (install)
  (binding [t/*opts* (assoc-in t/*opts* [:extra-env "CLOJURE_LOAD_PATH"]
                               (str/join (System/getProperty "path.separator")
                                         ["src" "resources" "test"]))]
    (t/cljr "-m" :portal.test-clr)))

(defn test* []
  (future (cljs-runtime "1.10.773"))
  (future (cljs-runtime "1.10.844"))
  (future
    (build)
    (future (t/clj "-M:test" "-m" :portal.test-runner))
    (future (t/bb "-m" :portal.test-runner))
    (future (cljr))))

(defn test "Run all clj/s tests." [] (cljs) (clj))

(defn -main [] (test))
