(ns cljs-http.core
  (:import goog.net.XhrIo)
  (:require [cljs-http.util :as util]
            [cljs.core.async :as async]))

(defn request
  "Execute the HTTP request corresponding to the given Ring request
  map and return a core.async channel."
  [{:keys [request-method headers body with-credentials?] :as request}]
  (let [channel (async/chan)
        request-url (util/build-url request)
        method (name (or request-method :get))
        timeout (or (:timeout request) 0)
        headers (util/build-headers headers)
        send-credentials (if (nil? with-credentials?)
                             true
                             with-credentials?)]
    (XhrIo.send request-url
     #(let [target (.-target %1)]
        (->> {:status (.getStatus target)
              :body (.getResponseText target)
              :headers (util/parse-headers (.getAllResponseHeaders target))
              :trace-redirects [request-url (.getLastUri target)]}
             (async/put! channel))
        (async/close! channel))
     method body headers timeout send-credentials)
    channel))
