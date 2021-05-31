(ns portal.runtime.fs-test
  (:require [clojure.test :refer [deftest is]]
            [portal.runtime.fs :as fs]))

(deftest fs
  (is (some? (fs/slurp "deps.edn")))
  (let [deps (fs/join (fs/cwd) "deps.edn")]
    (is (= (fs/exists deps) deps)))
  (is (some? (fs/home)))
  (is (some? (seq (fs/paths))))
  (is (contains?
       (into #{} (fs/list (fs/cwd)))
       (fs/join (fs/cwd) "deps.edn"))))
