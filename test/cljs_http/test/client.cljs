(ns cljs-http.test.client
  (:require-macros [cemerick.cljs.test :refer [is deftest]])
  (:require [cemerick.cljs.test :as t]
            [cljs-http.client :as client]))

(deftest test-parse-query-params
  (is (nil? (client/parse-query-params nil)))
  (is (nil? (client/parse-query-params "")))
  (is (= {:a "1"} (client/parse-query-params "a=1")))
  (is (= {:a "1" :b "2"} (client/parse-query-params "a=1&b=2"))))

(deftest test-parse-url
  (let [request (client/parse-url "http://example.com/test?a=1&b=2")]
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (nil? (:server-port request)))
    (is (= "/test" (:uri request)))
    (is (= "a=1&b=2" (:query-string request)))
    (is (= {:a "1" :b "2"} (:query-params request)))))
