(ns cljs-http.test.util
  (:use [cljs-http.util :only (android? url-encode)]))

(defn test-android? []
  (assert (not (android?))))

(defn test-url-encode []
  (assert (= "" (url-encode "")))
  (assert (= "x" (url-encode "x")))
  (assert (= "1%3D2%263%21%A7%24" (url-encode "1=2&3!§$"))))

(defn test []
  (test-android?)
  (test-url-encode))