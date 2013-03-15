(ns cljs-http.util
  (:require [clojure.string :refer [split lower-case]]
            [goog.userAgent :as agent]
            [goog.Uri :as Uri]))

(defn build-url
  "Build the url from the request map."
  [{:keys [scheme server-name server-port uri query-string]}]
  (str (doto (goog.Uri.)
         (. (setScheme (name scheme)))
         (. (setDomain server-name))
         (. (setPort server-port))
         (. (setPath uri))
         (. (setQuery query-string true)))))

(defn user-agent
  "Returns the user agent."
  [] (agent/getUserAgentString))

(defn android?
  "Returns true if the user agent is an Android client."
  [] (re-matches #"(?i).*android.*" (user-agent)))

(defn url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [unencoded] (js/encodeURIComponent unencoded))

(defn url-decode
  "Returns an UTF-8 URL decoded version of the given string."
  [encoded] (js/decodeURIComponent encoded))

(defn parse-headers [headers]
  (reduce
   #(let [[k v] (split %2 #":\s+")]
      (assoc %1 (lower-case k) v))
   {} (split (or headers "") #"\n")))
