(ns cljs-http.test.client
  (:require-macros [cemerick.cljs.test :refer [is deftest]])
  (:require [cemerick.cljs.test :as t]
            [cljs-http.client :as client]
            [cljs-http.util :as util]))

(deftest test-parse-query-params
  (is (nil? (client/parse-query-params nil)))
  (is (nil? (client/parse-query-params "")))
  (is (= {:a "1"} (client/parse-query-params "a=1")))
  (is (= {:a "1" :b "2"} (client/parse-query-params "a=1&b=2"))))

(deftest test-parse-url
  (is (nil? (client/parse-url nil)))
  (let [request (client/parse-url "http://example.com/test?a=1&b=2")]
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (nil? (:server-port request)))
    (is (= "/test" (:uri request)))
    (is (= "a=1&b=2" (:query-string request)))
    (is (= {:a "1" :b "2"} (:query-params request)))))

(deftest test-wrap-basic-auth
  (let [request {:request-method :get :url "/"}]
    ((client/wrap-basic-auth
      (fn [request]
        (is (nil? (get-in request [:headers "authorization"]))))) request)
    ((client/wrap-basic-auth
      (fn [request]
        (is (= "Basic dGlnZXI6c2NvdGNo" (get-in request [:headers "authorization"]))))
      ["tiger" "scotch"]) request)
    ((client/wrap-basic-auth
      (fn [request]
        (is (= "Basic dGlnZXI6c2NvdGNo" (get-in request [:headers "authorization"]))))
      {:username "tiger" :password "scotch"}) request)
    ((client/wrap-basic-auth
      (fn [request]
        (is (= "Basic dGlnZXI6c2NvdGNo" (get-in request [:headers "authorization"])))))
     (assoc request :basic-auth ["tiger" "scotch"]))))

(deftest test-wrap-content-type
  (let [request {:request-method :get :url "/"}]
    ((client/wrap-content-type
      (fn [request]
        (is (nil? (get-in request [:headers "content-type"]))))) request)
    ((client/wrap-content-type
      (fn [request]
        (is (= "application/edn" (get-in request [:headers "content-type"]))))
      "application/edn") request)
    ((client/wrap-content-type
      (fn [request]
        (is (= "application/edn" (get-in request [:headers "content-type"])))))
     (assoc request :content-type "application/edn"))))

(deftest test-wrap-edn-params
  (let [request ((client/wrap-edn-params identity) {:edn-params {:a 1}})]
    (is (= "application/edn" (get-in request [:headers "content-type"])))
    (is (= (pr-str {:a 1} ) (-> request :body)))))

(deftest test-wrap-json-params
  (let [request ((client/wrap-json-params identity) {:json-params {:a 1}})]
    (is (= "application/json" (get-in request [:headers "content-type"])))
    (is (= (util/json-encode {:a 1}) (-> request :body)))))

(deftest test-wrap-url
  (let [request {:request-method :get :url "http://example.com/?b=2" :query-params {:a "1"}}]
    ((client/wrap-url
      (fn [request]
        (is (= :get (:request-method request)))
        (is (= :http (:scheme request)))
        (is (= "example.com" (:server-name request)))
        (is (= "/" (:uri request)))
        (is (= {:a "1" :b "2"} (:query-params request)))))
     request)))
