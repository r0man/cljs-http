(ns cljs-http.test.util
  (:require-macros [cemerick.cljs.test :refer [are is deftest]])
  (:require [cemerick.cljs.test :as t]
            [cljs-http.util :as util]))

(deftest test-base64-encode
  (is (= "" (util/base64-encode "")))
  (is (= "eA==" (util/base64-encode "x")))
  (is (= "eA.." (util/base64-encode "x" true))))

(deftest test-base64-decode
  (is (= "" (util/base64-decode "")))
  (is (= "x" (util/base64-decode "eA==")))
  (is (= "x" (util/base64-decode "eA.." true))))

(deftest test-basic-auth
  (is (nil? (util/basic-auth nil)))
  (is (= "Basic Og==" (util/basic-auth ["" ""])))
  (is (= "Basic dGlnZXI6c2NvdGNo"
         (util/basic-auth ["tiger" "scotch"])))
  (is (= "Basic dGlnZXI6c2NvdGNo"
         (util/basic-auth {:username "tiger" :password "scotch"}))))

;; (deftest test-android?
;;   (android?))

(deftest test-build-url
  (is (= "https://localhost:80/continents?page=1"
         (util/build-url {:scheme :https
                          :server-name "localhost"
                          :server-port 80
                          :uri "/continents"
                          :query-string "page=1"}))))

(deftest test-camelize
  (are [s expected]
    (is (= expected (util/camelize s)))
    "" ""
    "accept" "Accept"
    "content-type" "Content-Type"))

(deftest test-json-encode
  (are [x expected]
    (is (= expected (util/json-encode x)))
    nil "null"
    1 "1"
    {:a 1} "{\"a\":1}"))

(deftest test-json-decode
  (are [x expected]
    (is (= expected (util/json-decode x)))
    nil nil
    "null" nil
    "1" 1
    "{\"a\":1}" {:a 1}))

(deftest test-url-encode
  (is (= "" (util/url-encode "")))
  (is (= "x" (util/url-encode "x")))
  (is (= "1%3D2%263!%C2%A7%24" (util/url-encode "1=2&3!§$"))))

(deftest test-user-agent
  (util/user-agent))

(deftest test-parse-headers
  (are [headers expected]
    (is (= expected (util/parse-headers headers)))
    "" {}
    "Content-Type: application/edn\nContent-Length: 10"
    {"content-type" "application/edn", "content-length" "10"}))
