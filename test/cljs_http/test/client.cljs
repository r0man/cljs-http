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

(deftest test-wrap-basic-auth
  (let [request {:request-method :get :url "/"}]
    ((client/wrap-basic-auth
      (fn [request]
        (is (nil? (-> request :headers "Authorization"))))) request)
    ((client/wrap-basic-auth
      (fn [request]
        (is (= "Basic dGlnZXI6c2NvdGNo" (-> request :headers "Authorization"))))
      ["tiger" "scotch"]) request)
    ((client/wrap-basic-auth
      (fn [request]
        (is (= "Basic dGlnZXI6c2NvdGNo" (-> request :headers "Authorization"))))
      {:username "tiger" :password "scotch"}) request)
    ((client/wrap-basic-auth
      (fn [request]
        (is (= "Basic dGlnZXI6c2NvdGNo" (-> request :headers "Authorization")))))
     (assoc request :basic-auth ["tiger" "scotch"]))))

(deftest test-wrap-content-type
  (let [request {:request-method :get :url "/"}]
    ((client/wrap-content-type
      (fn [request]
        (is (nil? (-> request :headers "Content-Type"))))) request)
    ((client/wrap-content-type
      (fn [request]
        (is (= "application/edn" (-> request :headers "Content-Type"))))
      "application/edn") request)
    ((client/wrap-content-type
      (fn [request]
        (is (= "application/edn" (-> request :headers "Content-Type")))))
     (assoc request :content-type "application/edn"))))
