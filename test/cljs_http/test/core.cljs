(ns cljs-http.test.core
  (:require-macros [cemerick.cljs.test :refer [are is deftest testing]])
  (:require [cemerick.cljs.test :as t]
            [cljs-http.core :as core]))

(deftest test-build-xhr
  (testing "default headers are applied on xhr object"
    (let [xhr (core/build-xhr {:default-headers {"x-csrf-token" "abc123"}})
          headers (js->clj (.toObject (.-headers xhr)))]
      (is (= {"X-Csrf-Token" "abc123"} headers)))))
