(ns cljs-http.test.client
  (:require-macros [cemerick.cljs.test :refer [is deftest]])
  (:require [cemerick.cljs.test :as t]
            [cljs-http.client :as client]))

(deftest test-parse-url
  (let [request (client/parse-url "http://example.com/test")]
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (nil? (:server-port request)))
    (is (= "/test" (:uri request)))
    (is (nil? (:query-data request)))
    (is (nil? (:query-string request)))))
