(ns cljs-http.client
  (:refer-clojure :exclude (get))
  (:require [cljs-http.core :as core]
            [cljs-http.util :as util]
            [goog.json :as json]
            [goog.Uri :as uri]
            [goog.userAgent :as user-agent])
  (:use [clojure.string :only (blank? join)]))

(defn android? []
  (re-matches #"(?i).*android.*" (user-agent/getUserAgentString)))

(defn if-pos [v]
  (if (and v (pos? v)) v))

(defn parse-url [url]
  (let [uri (uri/parse url)]
    {:scheme (. uri (getScheme))
     :server-name (. uri (getDomain))
     :server-port (if-pos (. uri (getPort)))
     :uri (. uri (getPath))
     :query-data (. uri (getQueryData))
     :query-string (str (. uri (getQueryData)))}))

(def unexceptional-status?
  #{200 201 202 203 204 205 206 207 300 301 302 303 307})

(defn generate-query-string [params]
  (join "&" (map (fn [[k v]] (str (util/url-encode (name k)) "=" (util/url-encode (str v)))) params)))

(defn wrap-query-params [client]
  (fn [{:keys [query-params] :as req}]
    (if query-params
      (client (-> req (dissoc :query-params)
                  (assoc :query-string
                    (generate-query-string query-params))))
      (client req))))

(defn wrap-android-cors-bugfix [client]
  (fn [request]
    (client
     (if (android?)
       (assoc-in request [:query-params :android] (Math/random))
       request))))

(defn wrap-json-response [client]
  (fn [{:keys [complete-fn] :as req}]
    (client
     (assoc req
       :complete-fn
       (fn [{:keys [body] :as response}]
         (if complete-fn
           (complete-fn
            (if-not (blank? body)
              (assoc response :body (json/parse body))
              response))))))))

(defn wrap-js->clj [client]
  (fn [{:keys [complete-fn] :as req}]
    (client
     (assoc req
       :complete-fn
       (fn [{:keys [body] :as response}]
         (if complete-fn
           (complete-fn
            (if body
              (assoc response :body (js->clj body :keywordize-keys true))
              response))))))))

(defn wrap-deserialization [client]
  (fn [{:keys [complete-fn deserialize] :as req}]
    (client
     (assoc req
       :complete-fn
       (fn [{:keys [body] :as response}]
         (if complete-fn
           (complete-fn
            (if deserialize
              (assoc response :body (deserialize body))
              response))))))))

(defn wrap-on-success [client]
  (fn [{:keys [on-success complete-fn] :as req}]
    (client
     (assoc req
       :complete-fn
       (fn [response]
         (if (and on-success (unexceptional-status? (:status response)))
           (on-success response)
           (if complete-fn (complete-fn response))))))))

(defn wrap-on-error [client]
  (fn [{:keys [on-error complete-fn] :as req}]
    (client
     (assoc req
       :complete-fn
       (fn [response]
         (if (and on-error (not (unexceptional-status? (:status response))))
           (on-error response)
           (if complete-fn (complete-fn response))))))))

(defn wrap-method [client]
  (fn [req]
    (if-let [m (:method req)]
      (client (-> req (dissoc :method)
                  (assoc :request-method m)))
      (client req))))

(defn wrap-url [client]
  (fn [req]
    (if-let [url (:url req)]
      (client (-> req (dissoc :url) (merge (parse-url url))))
      (client req))))

(defn wrap-request
  "Returns a battaries-included HTTP request function coresponding to the given
   core client. See client/client."
  [request]
  (-> request
      wrap-query-params
      wrap-android-cors-bugfix
      wrap-json-response
      wrap-js->clj
      wrap-deserialization
      wrap-on-success
      wrap-on-error
      wrap-method
      wrap-url))

(def #^{:doc
        "Executes the HTTP request corresponding to the given map and returns the
   response map for corresponding to the resulting HTTP response.

   In addition to the standard Ring request keys, the following keys are also
   recognized:
   * :url
   * :method
   * :query-params
   * :basic-auth
   * :content-type
   * :accept
   * :accept-encoding
   * :as

  The following additional behaviors over also automatically enabled:
   * Exceptions are thrown for status codes other than 200-207, 300-303, or 307
   * Gzip and deflate responses are accepted and decompressed
   * Input and output bodies are coerced as required and indicated by the :as
     option."}
  request
  (wrap-request core/request))

(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  [url on-success & [on-error & {:as options}]]
  (request (merge options {:method :get :on-error on-error :on-success on-success :url url})))

(defn head
  "Like #'request, but sets the :method and :url as appropriate."
  [url on-success & [on-error & {:as options}]]
  (request (merge options {:method :head :on-error on-error :on-success on-success :url url})))

(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  [url on-success & [on-error & {:as options}]]
  (request (merge options {:method :post :on-error on-error :on-success on-success :url url})))

(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  [url on-success & [on-error & {:as options}]]
  (request (merge options {:method :put :on-error on-error :on-success on-success :url url})))

(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  [url on-success & [on-error & {:as options}]]
  (request (merge options {:method :delete :on-error on-error :on-success on-success :url url})))
