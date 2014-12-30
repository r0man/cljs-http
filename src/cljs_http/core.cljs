(ns cljs-http.core
  (:import [goog.net EventType XhrIo])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.util :as util]
            [cljs.core.async :as async]))

(def pending-requests (atom {}))

(defn abort!
  "Attempt to close the given channel and abort the pending HTTP request
  with which it is associated."
  [channel]
  (when-let [xhr (@pending-requests channel)]
    (swap! pending-requests dissoc channel)
    (async/close! channel)
    (.abort xhr)))

(defn- aborted? [xhr]
  (= (.getLastErrorCode xhr) goog.net.ErrorCode.ABORT))

(defn request
  "Execute the HTTP request corresponding to the given Ring request
  map and return a core.async channel."
  [{:keys [request-method headers body with-credentials? cancel] :as request}]
  (let [channel (async/chan)
        request-url (util/build-url request)
        method (name (or request-method :get))
        timeout (or (:timeout request) 0)
        headers (util/build-headers headers)
        send-credentials (if (nil? with-credentials?)
                           true
                           with-credentials?)
        xhr (doto (XhrIo.)
              (.setTimeoutInterval timeout)
              (.setWithCredentials send-credentials))]
    (swap! pending-requests assoc channel xhr)
    (.listen xhr EventType.COMPLETE
             (fn [evt]
               (let [target (.-target evt)
                     response {:status (.getStatus target)
                               :success (.isSuccess target)
                               :body (.getResponseText target)
                               :headers (util/parse-headers (.getAllResponseHeaders target))
                               :trace-redirects [request-url (.getLastUri target)]}]
                 (if-not (aborted? xhr)
                   (async/put! channel response))
                 (swap! pending-requests dissoc channel)
                 (if cancel (async/close! cancel))
                 (async/close! channel))))
    (.send xhr request-url method body headers)
    (if cancel
      (go
        (let [v (async/<! cancel)]
          (if (not (.isComplete xhr))
            (.abort xhr)))))
    channel))
