(ns cljs-http.core
  (:require [cljs-http.util :as util]
            [goog.labs.net.xhr :as xhr]))

(defn request
  "Executes the HTTP request corresponding to the given Ring request
  map and return a SimpleResult."
  [{:keys [request-method headers body credendials] :as request}]
  (let [method (name (or request-method :get))
        url (util/build-url request)
        options (clj->js {:headers headers :withCredentials credendials})]
    (xhr/send method url body options)))
