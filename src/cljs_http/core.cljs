(ns cljs-http.core
  (:import [goog.net EventType ErrorCode XhrIo]
           [goog.net Jsonp]
           [goog.net.XhrIo ResponseType])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.util :as util]
            [cljs.core.async :as async]
            [clojure.string :as s]))

(def pending-requests (atom {}))

(defn abort!
  "Attempt to close the given channel and abort the pending HTTP request
  with which it is associated."
  [channel]
  (when-let [req (@pending-requests channel)]
    (swap! pending-requests dissoc channel)
    (async/close! channel)
    (if (.hasOwnProperty req "abort")
      (.abort req)
      (.cancel (:jsonp req) (:request req)))))

(defn- aborted? [xhr]
  (= (.getLastErrorCode xhr) goog.net.ErrorCode.ABORT))

(defn apply-default-headers!
  "Takes an XhrIo object and applies the default-headers to it."
  [xhr headers]
  (doseq [h-name (map util/camelize (keys headers))
          h-val (vals headers)]
    (.set (.-headers xhr) h-name h-val)))

(defn build-xhr
  "Builds an XhrIo object from the request parameters."
  [{:keys [with-credentials? response-type default-headers] :as request
    :or {response-type goog.net.XhrIo.ResponseType.DEFAULT}}]
  (let [timeout (or (:timeout request) 0)
        send-credentials (if (nil? with-credentials?)
                           true
                           with-credentials?)]
    (doto (XhrIo.)
          (apply-default-headers! default-headers)
          (.setTimeoutInterval timeout)
          (.setWithCredentials send-credentials)
          (.setResponseType response-type))))

;; Reverses the goog.net.ErrorCode constants to map to CLJS keywords
(def error-kw
  (let [kebabize (fn [s]
                   (-> (s/lower-case s)
                       (s/replace #"_" "-")))]
    (->> (js->clj goog.net.ErrorCode)
         (keep (fn [[code-name n]]
                 (when (integer? n)
                   [n (keyword (kebabize code-name))])))
         (into {}))))

(defn xhr
  "Execute the HTTP request corresponding to the given Ring request
  map and return a core.async channel."
  [{:keys [request-method headers body response-type with-credentials? cancel] :as request}]
  (let [channel (async/chan)
        request-url (util/build-url request)
        method (name (or request-method :get))
        headers (util/build-headers headers)
        xhr (build-xhr request)]
    (swap! pending-requests assoc channel xhr)
    (.listen xhr EventType.COMPLETE
             (fn [evt]
               (let [target (.-target evt)
                     response {:status (.getStatus target)
                               :success (.isSuccess target)
                               :body (if response-type
                                       (.getResponse target)
                                       (.getResponseText target))
                               :headers (util/parse-headers (.getAllResponseHeaders target))
                               :trace-redirects [request-url (.getLastUri target)]
                               :error-code (error-kw (.getLastErrorCode target))
                               :error-text (.getLastError target)}]
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

(defn jsonp
  "Execute the JSONP request corresponding to the given Ring request
  map and return a core.async channel."
  [{:keys [timeout callback-name cancel] :as request}]
  (let [channel (async/chan)
        jsonp (Jsonp. (util/build-url request) callback-name)]
    (.setRequestTimeout jsonp timeout)
    (let [req (.send jsonp nil
                     (fn success-callback [data]
                       (let [response {:status 200
                                       :success true
                                       :body (js->clj data :keywordize-keys true)}]
                         (async/put! channel response)
                         (swap! pending-requests dissoc channel)
                         (if cancel (async/close! cancel))
                         (async/close! channel)))
                     (fn error-callback []
                         (swap! pending-requests dissoc channel)
                         (if cancel (async/close! cancel))
                         (async/close! channel)))]
      (swap! pending-requests assoc channel {:jsonp jsonp :request req})
      (if cancel
        (go
          (let [v (async/<! cancel)]
            (.cancel jsonp req)))))
    channel))

(defn request
  "Execute the HTTP request corresponding to the given Ring request
  map and return a core.async channel."
  [{:keys [request-method] :as request}]
  (if (= request-method :jsonp)
    (jsonp request)
    (xhr request)))
