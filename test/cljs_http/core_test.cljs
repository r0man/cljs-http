(ns cljs-http.core-test
  (:require [cljs-http.core :as core]
            [cljs.test :refer-macros [is deftest testing]]))

(deftest test-build-xhr
  (testing "default headers are applied on xhr object"
    (let [xhr (core/build-xhr {:default-headers {"x-csrf-token" "abc123"
                                                 "x-requested-with" "XMLHttpRequest"}})
          headers (js->clj (.toObject (.-headers xhr)))]
      (is (= {"X-Csrf-Token" "abc123"
              "X-Requested-With" "XMLHttpRequest"} headers)))))
