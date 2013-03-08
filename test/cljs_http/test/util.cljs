(ns cljs-http.test.util
  (:require-macros [cemerick.cljs.test :refer [is deftest]])
  (:require [cemerick.cljs.test :as t]
            [cljs-http.util :refer [android? build-url url-encode user-agent]]))

;; (deftest test-android?
;;   (android?))

(deftest test-build-url
  (is (= "https://localhost:80/continents?page=1"
         (build-url {:scheme :https
                     :server-name "localhost"
                     :server-port 80
                     :uri "/continents"
                     :query-string "page=1"}))))

(deftest test-url-encode
  (is (= "" (url-encode "")))
  (is (= "x" (url-encode "x")))
  (is (= "1%3D2%263!%C2%A7%24" (url-encode "1=2&3!§$"))))

(deftest test-user-agent
  (user-agent))
