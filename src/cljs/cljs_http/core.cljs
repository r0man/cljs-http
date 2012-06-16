(ns cljs-http.core
  (:require [goog.events :as events]
            [goog.Uri :as Uri]
            [goog.net.EventType :as EventType]
            [goog.net.XhrIo :as XhrIo]
            [goog.structs.Map :as Map]))

(defn build-url
  "Build the url from the request map."
  [{:keys [scheme server-name server-port uri query-string]}]
  (str (doto (goog.Uri.)
         (. (setScheme scheme))
         (. (setDomain server-name))
         (. (setPort server-port))
         (. (setPath uri))
         (. (setQuery query-string)))))

(defn request
  "Executes the HTTP request corresponding to the given Ring request
   map and calls the on-complete fn with the Ring response map
   corresponding to the resulting HTTP response.

   Note that where Ring uses InputStreams for the request and response
   bodies, the cljs-http library uses JavaScript strings for the
   bodies."
  [{:keys [request-method scheme server-name server-port uri query-string
           headers content-type character-encoding body on-complete] :as request}]
  (prn request-method)
  (prn server-name)
  (prn server-port)
  (prn uri)
  (let [xhr (goog.net.XhrIo.)]
    (.setWithCredentials xhr true)
    (if on-complete
      (events/listen
       xhr goog.net.EventType.COMPLETE
       #(try
          (on-complete
           {:status (. xhr (getStatus))
            :body (. xhr (getResponseText))
            :headers (or (. xhr (getAllResponseHeaders)) {})})
          (catch js/Error e
            (.log js/console e)
            (.log js/console (. e -stack)))
          (finally
           (. xhr (dispose))))))
    (. xhr (send (build-url request)
                 (name (or request-method :get))
                 body))))
