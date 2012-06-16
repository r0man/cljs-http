(ns cljs-http.test.core
  (:use [cljs-http.core :only (build-url)]))

(defn test-build-url []
  (build-url {:scheme :https
              :server-name "localhost"
              :server-port 80
              :uri "/continents"
              :query-string "page=1"}))

(defn test []
  (test-build-url))
