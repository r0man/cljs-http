(ns cljs-http.util
  (:import goog.Uri)
  (:require [clojure.string :refer [blank? split lower-case]]
            [goog.crypt.base64 :as base64]
            [goog.userAgent :as agent]))

(defn base64-encode
  "Base64-encode a String."
  [s & [web-safe]]
  (base64/encodeString s web-safe))

(defn base64-decode
  "Base64-decode a String."
  [s & [web-safe]]
  (base64/decodeString s web-safe))

(defn basic-auth
  "Returns the value of the HTTP basic authentication header for
  `credentials`."
  [credentials]
  (let [[username password]
        (if (map? credentials)
          (map credentials [:username :password])
          credentials)]
    (if username (str "Basic " (base64-encode (str username ":" password))))))

(defn build-url
  "Build the url from the request map."
  [{:keys [scheme server-name server-port uri query-string]}]
  (str (doto (Uri.)
         (.setScheme (name (or scheme :http)))
         (.setDomain server-name)
         (.setPort server-port)
         (.setPath uri)
         (.setQuery query-string true))))

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
      (if (or (blank? k) (blank? v))
        %1 (assoc %1 (lower-case k) v)))
   {} (split (or headers "") #"\n")))
