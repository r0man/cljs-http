(ns cljs-http.core
  (:require [goog.events :as events]
            [goog.Uri :as Uri]
            [goog.net.EventType :as EventType]
            [goog.net.XhrIo :as XhrIo]
            [goog.structs.Map :as Map]))

(defn request
  "Executes the HTTP request corresponding to the given Ring request
   map and calls the complete-fn with the Ring response map
   corresponding to the resulting HTTP response.

   Note that where Ring uses InputStreams for the request and response
   bodies, the cljs-http library uses JavaScript Strings for the
   bodies."
  [{:keys [request-method scheme server-name server-port uri query-string
           headers content-type character-encoding body complete-fn] :as request}]
  (let [xhr (goog.net.XhrIo.)
        url (str (doto (goog.Uri.)
                   (. (setScheme scheme))
                   (. (setDomain server-name))
                   (. (setPort server-port))
                   (. (setPath uri))
                   (. (setQuery query-string))))]
    (if complete-fn
      (events/listen
       xhr goog.net.EventType.COMPLETE
       #(try
          (complete-fn
           {:status (. xhr (getStatus))
            :body (. xhr (getResponseText))
            :headers (or (. xhr (getAllResponseHeaders)) {})})
          (catch js/Error e
            (.log js/console e)
            (.log js/console (. e -stack)))
          (finally
           (. xhr (dispose))))))
    (. xhr (send url (name (or request-method :get))))))
