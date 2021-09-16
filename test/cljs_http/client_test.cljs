(ns cljs-http.client-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [cljs-http.client :as client]
            [cljs-http.util :as util]
            [cljs.test :refer-macros [async is deftest testing]]))

(deftest test-parse-query-params
  (is (nil? (client/parse-query-params nil)))
  (is (nil? (client/parse-query-params "")))
  (is (= {:a "1"} (client/parse-query-params "a=1")))
  (is (= {:a "1" :b "2"} (client/parse-query-params "a=1&b=2")))
  (is (= {:a ["1" "2"]} (client/parse-query-params "a=1&a=2"))))

(deftest test-generate-query-string
  (is (= (client/generate-query-string {}) ""))
  (is (= (client/generate-query-string {:a 1}) "a=1"))
  (is (= (client/generate-query-string (sort {:a 1 :b 2})) "a=1&b=2"))
  (is (= (client/generate-query-string {:a [1 2]}) "a=1&a=2")))

(deftest test-parse-url
  (is (nil? (client/parse-url nil)))
  (let [request (client/parse-url "http://example.com/test?a=1&b=2")]
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (nil? (:server-port request)))
    (is (= "/test" (:uri request)))
    (is (= "a=1&b=2" (:query-string request)))
    (is (= {:a "1" :b "2"} (:query-params request)))))

(deftest test-wrap-accept
  (let [request {:request-method :get :url "/"}]
    ((client/wrap-accept
      (fn [request]
        (is (nil? (get-in request [:headers "accept"]))))) request)
    ((client/wrap-accept
      (fn [request]
        (is (= "application/edn" (get-in request [:headers "accept"]))))
      "application/edn") request)
    ((client/wrap-accept
      (fn [request]
        (is (= "application/edn" (get-in request [:headers "accept"])))))
     (assoc request :accept "application/edn"))))

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

(deftest test-wrap-transit-params
  (let [request ((client/wrap-transit-params identity) {:transit-params {:a 1}})]
    (is (= "application/transit+json" (get-in request [:headers "content-type"])))
    (is (= (util/transit-encode {:a 1} :json nil) (-> request :body)))))

(deftest test-wrap-edn-params
  (let [request ((client/wrap-edn-params identity) {:edn-params {:a 1}})]
    (is (= "application/edn" (get-in request [:headers "content-type"])))
    (is (= (pr-str {:a 1} ) (-> request :body)))))

(deftest test-wrap-json-params
  (let [request ((client/wrap-json-params identity) {:json-params {:a 1}})]
    (is (= "application/json" (get-in request [:headers "content-type"])))
    (is (= (util/json-encode {:a 1}) (-> request :body)))))

(deftest test-override-headers
  (let [content-type "application/test"
        request ((client/wrap-json-params identity) {:json-params {:a 1}})
        edn-request ((client/wrap-edn-params identity) {:edn-params {:a 1} :headers {"content-type" content-type}})
        transit-request ((client/wrap-transit-params identity) {:transit-params {:a 1} :headers {"content-type" content-type}})
        json-request ((client/wrap-json-params identity) {:json-params {:a 1} :headers {"content-type" content-type}})
        form-request ((client/wrap-form-params identity) {:form-params {:a 1} :headers {"content-type" content-type}})]
    (is (= "application/json" (get-in request [:headers "content-type"])))
    (is (= content-type (get-in edn-request [:headers "content-type"])))
    (is (= content-type (get-in transit-request [:headers "content-type"])))
    (is (= content-type (get-in json-request [:headers "content-type"])))
    (is (= content-type (get-in form-request [:headers "content-type"])))))

(deftest test-wrap-default-headers
  (let [request ((client/wrap-default-headers identity) {:default-headers {"X-Csrf-Token" "abc"}})]
    (is (= "abc" (get-in request [:default-headers "X-Csrf-Token"])))))

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

(deftest test-wrap-form-params
  (testing "With form params"
    (let [request {:request-method :post :form-params (sorted-map :param1 "value1" :param2 "value2")}
          response ((client/wrap-form-params identity) request)]
      (is (= "param1=value1&param2=value2" (:body response)))
      (is (= "application/x-www-form-urlencoded" (get-in response [:headers "content-type"])))
      (is (not (contains? response :form-params))))
    (let [request {:request-method :put :form-params (sorted-map :param1 "value1" :param2 "value2")}
          response ((client/wrap-form-params identity) request)]
      (is (= "param1=value1&param2=value2" (:body response)))
      (is (= "application/x-www-form-urlencoded" (get-in response [:headers "content-type"])))
      (is (not (contains? response :form-params))))
    (let [request {:request-method :put :form-params (sorted-map :param1 [1 2 3] :param2 "value2")}
          response ((client/wrap-form-params identity) request)]
      (is (= "param1=1&param1=2&param1=3&param2=value2" (:body response)))
      (is (= "application/x-www-form-urlencoded" (get-in response [:headers "content-type"])))
      (is (not (contains? response :form-params)))))
  (testing "Ensure it does not affect GET requests"
    (let [request {:request-method :get :body "untouched" :form-params {:param1 "value1" :param2 "value2"}}
          response ((client/wrap-form-params identity) request)]
      (is (= "untouched" (:body response)))
      (is (not (contains? (:headers response) "content-type")))))
  (testing "with no form params"
    (let [request {:body "untouched"}
          response ((client/wrap-form-params identity) request)]
      (is (= "untouched" (:body response)))
      (is (not (contains? (:headers response) "content-type"))))))

(deftest test-custom-channel
  (let [c (async/chan 1)
        request-no-chan {:request-method :get :url "http://localhost/"}
        request-with-chan {:request-method :get :url "http://localhost/" :channel c}]
    (testing "request api with middleware"
      (is (not= c (client/request request-no-chan)))
      (is (= c (client/request request-with-chan))))))

(deftest ^:async test-cancel-channel
  (let [cancel (async/chan 1)
        request (client/request {:request-method :get :url "http://google.com" :cancel cancel})]
    (async/close! cancel)
    (testing "output channel is closed if request is cancelled"
      (async done
        (go
          (let [resp (async/<! request)]
            (is (= resp nil)))
          (done))))))

;; See http://doc.jsfiddle.net/use/echo.html for details on the endpoint used
;; for JSONP tests
(deftest ^:async test-cancel-jsonp-channel
  (let [cancel (async/chan 1)
        request (client/request {:request-method :jsonp :url "http://jsfiddle.net/echo/jsonp/" :cancel cancel})]
    (async/close! cancel)
    (testing "output channel is closed if request is cancelled"
      (async done
        (go
          (let [resp (async/<! request)]
            (is (= resp nil)))
          (done))))))

(deftest ^:async test-jsonp
  (let [request (client/jsonp "http://jsfiddle.net/echo/jsonp/"
                              {:query-params {:foo "bar"}
                               :channel (async/chan 1 (map :body))})]
    (testing "jsonp request"
      (async done
        (go
          (let [resp (async/<! request)]
            (is (= (:foo resp) "bar")))
          (done))))))

(deftest ^:async test-keywordize-jsonp
  (let [request (client/jsonp "http://jsfiddle.net/echo/jsonp/"
                              {:keywordize-keys? false
                               :query-params {:foo ""}
                               :channel (async/chan 1 (map :body))})]
    (testing "JSON-P response keys aren't converted to keywords"
      (async done
        (go
          (let [resp (async/<! request)]
            (is (every? string? (keys resp))))
          (done))))))

#_(deftest ^:async test-progress-channel-returns-progress
  (let [progress (async/chan 1)
        _ (client/post "http://www.google.com"
                       {:json-params {:foo :bar}
                        :progress    progress})]
    (testing "progress channel returns progress"
      (async done
        (go
          (let [return (async/<! progress)]
            (is (map? return))
            (is (= :upload (:direction return)))
            (is (contains? return :total))
            (is (contains? return :loaded)))
          (done))))))

(deftest test-decode-body
  (let [headers {"content-type" "application/transit+json"}
        body "[\"^ \",\"~:a\",1]"
        decode-fn #(util/transit-decode % :json nil)]
    (testing "application/transit+json response"
      (is (= {:status 200 :body {:a 1} :headers headers}
             (client/decode-body {:status 200
                                  :body body
                                  :headers headers}
                                 decode-fn
                                 "application/transit+json"
                                 :get))))

    (testing "text/plain response"
      (is (= {:status 200 :body body :headers {"content-type" "text/plain"}}
             (client/decode-body {:status 200
                                  :body body
                                  :headers {"content-type" "text/plain"}}
                                 decode-fn
                                 "application/transit+json"
                                 :get))))

    (testing "204 status"
      (is (= {:status 204 :body body :headers headers}
             (client/decode-body {:status 204 :body body :headers headers}
                                 decode-fn
                                 "application/transit+json"
                                 :get))))

    (testing ":head request-method"
      (is (= {:status 200 :body body :headers headers}
             (client/decode-body {:status 200 :body body :headers headers}
                                 decode-fn
                                 "application/transit+json"
                                 :head))))))

(deftest ^:async http-error-code
  (testing "Successful/unsuccessful response results in appropriate :error-code"
    (let [success-req (client/get "http://httpbin.org/get")
          timeout-req (client/get "http://httpbin.org/delay/10" {:timeout 1})]
      (async done
        (go
          (is (= :no-error (:error-code (async/<! success-req))))
          (is (= :timeout  (:error-code (async/<! timeout-req))))
          (done))))))

(deftest ^:async response-type
  (let [request (client/get "http://httpbin.org/image/png"
                            {:response-type :array-buffer})]
    (testing "Getting and reading arraybuffer response"
      (async done
        (go
          (let [resp (async/<! request)
                body (js/Uint8Array. (:body resp))
                sign (array-seq (.subarray body 0 8))]
            ;; PNG image signature
            (is (= [137 80 78 71 13 10 26 10] sign))
            (done)))))))
