(ns cljs-http.util)

(defn url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [unencoded] (js/escape unencoded))
