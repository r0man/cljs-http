(ns cljs-http.core
  (:import goog.net.XhrIo)
  (:require [cljs-http.util :as util]
            [cljs.core.async :as async]))

(defn request
  "Execute the HTTP request corresponding to the given Ring request
  map and return a core.async channel."
  [{:keys [request-method headers body] :as request}]
  (let [channel (async/chan)
        method (name (or request-method :get))
        timeout (or (:timeout request) 0)
        headers (util/build-headers headers)]
    (XhrIo/send
     (util/build-url request)
     #(let [target (.-target %1)]
        (->> {:status (.getStatus target)
              :body (.getResponseText target)
              :headers (util/parse-headers (.getAllResponseHeaders target))}
             (async/put! channel))
        (async/close! channel))
     method body headers timeout true)
    channel))
