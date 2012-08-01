(ns cljs-http.test.client
  (:require [cljs-http.client :as client]))

(defn test-parse-url []
  (let [request (client/parse-url "http://example.com/test")]
    (assert (= :http (:scheme request)))
    (assert (= "example.com" (:server-name request)))
    (assert (nil? (:server-port request)))
    (assert (= "/test" (:uri request)))
    (assert (nil? (:query-data request)))
    (assert (nil? (:query-string request)))))

(defn test []
  (test-parse-url))