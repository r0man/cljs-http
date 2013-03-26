(ns cljs-http.client
  (:import goog.labs.async.SimpleResult)
  (:refer-clojure :exclude [get])
  (:require [cljs-http.core :as core]
            [cljs-http.util :as util]
            [cljs.reader :refer [read-string]]
            [clojure.string :refer [blank? join split]]
            [goog.Uri :as uri]
            [goog.json :as json]
            [goog.labs.async.wait :as wait]))

(defn wait
  "Calls the handler on resolution of the result (success or failure)"
  [result handler] (wait/wait result handler))

(defn on-error
  "Calls the handler if the result action errors."
  [result handler] (wait/onError result #(handler (.getError %1))))

(defn on-success
  "Calls the handler if the result succeeds."
  [result handler] (wait/onSuccess result handler))

(defn if-pos [v]
  (if (and v (pos? v)) v))

(defn parse-query-params
  "Parse `s` as query params and return a hash map."
  [s]
  (if-not (blank? s)
    (reduce
     #(let [[k v] (split %2 #"=")]
        (assoc %1
          (keyword (util/url-decode k))
          (util/url-decode v)))
     {} (split (str s) #"&"))))

(defn parse-url
  "Parse `url` into a hash map."
  [url]
  (let [uri (uri/parse url)
        query-data (.getQueryData uri)]
    {:scheme (keyword (.getScheme uri))
     :server-name (.getDomain uri)
     :server-port (if-pos (.getPort uri))
     :uri (.getPath uri)
     :query-string (if-not (.isEmpty query-data)
                     (str query-data))
     :query-params (if-not (.isEmpty query-data)
                     (parse-query-params (str query-data)))}))

(def unexceptional-status?
  #{200 201 202 203 204 205 206 207 300 301 302 303 307})

(defn generate-query-string [params]
  (join "&" (map (fn [[k v]] (str (util/url-encode (name k)) "=" (util/url-encode (str v)))) params)))

(defn wrap-credentials [client credentials?]
  (fn [request]
    (client (assoc request :credentials credentials?))))

(defn- parse-xhr [xhr]
  {:body (.-response xhr)
   :headers (util/parse-headers (.getAllResponseHeaders xhr))
   :status (.-status xhr)})

(defn- parse-error [error]
  (parse-xhr (.-xhr error)))

(defn wrap-response
  "Wrap the XMLHttpRequest of `client` into a Ring response map."
  [client]
  (fn [request]
    (let [result (SimpleResult.)]
      (doto (client request)
        (on-success #(.setValue result (parse-xhr %1)))
        (on-error #(.setError result (parse-error %1))))
      result)))

(defn decode-body
  "Decocde the :body of `response` with `decode-fn` if the content type matches."
  [response decode-fn content-type]
  (if (re-find (re-pattern (format "(?i)%s" content-type))
               (str (clojure.core/get (:headers response) "content-type" "")))
    (update-in response [:body] decode-fn)
    response))

(defn wrap-edn-response
  "Decode application/clojure responses."
  [client]
  (fn [request]
    (let [result (SimpleResult.)
          decode #(decode-body %1 read-string "application/edn")]
      (doto (client request)
        (on-success #(.setValue result (decode %1)))
        (on-error #(.setError result (decode %1))))
      result)))

(defn- read-json [s]
  (js->clj (json/parse s) :keywordize-keys true))

(defn wrap-accept
  [client & [accept]]
  (fn [request]
    (if-let [accept (or (:accept request) accept)]
      (client (assoc-in request [:headers "Accept"] accept))
      (client request))))

(defn wrap-content-type
  [client & [content-type]]
  (fn [request]
    (if-let [content-type (or (:content-type request) content-type)]
      (client (assoc-in request [:headers "Content-Type"] content-type))
      (client request))))

(defn wrap-json-response
  "Decode application/json responses."
  [client]
  (fn [request]
    (let [result (SimpleResult.)
          decode #(decode-body %1 read-json "application/json")]
      (doto (client request)
        (on-success #(.setValue result (decode %1)))
        (on-error #(.setError result (decode %1))))
      result)))

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
     (if (util/android?)
       (assoc-in request [:query-params :android] (Math/random))
       request))))

(defn wrap-method [client]
  (fn [req]
    (if-let [m (:method req)]
      (client (-> req (dissoc :method)
                  (assoc :request-method m)))
      (client req))))

(defn wrap-server-name [client server-name]
  #(client (assoc %1 :server-name server-name)))

(defn wrap-url [client]
  (fn [req]
    (if-let [url (:url req)]
      (client (-> req (dissoc :url) (merge (parse-url url))))
      (client req))))

(defn wrap-basic-auth
  "Middleware converting the :basic-auth option or `credentials` into
  an Authorization header."
  [client & [credentials]]
  (fn [req]
    (let [credentials (or (:basic-auth req) credentials)]
      (if-not (empty? credentials)
        (client (-> (dissoc req :basic-auth)
                    (assoc-in [:headers "Authorization"] (util/basic-auth credentials))))
        (client req)))))

(defn wrap-request
  "Returns a battaries-included HTTP request function coresponding to the given
   core client. See client/client."
  [request]
  (-> request
      wrap-response
      wrap-edn-response
      wrap-json-response
      wrap-query-params
      wrap-android-cors-bugfix
      wrap-method
      wrap-url))

(def #^{:doc
        "Executes the HTTP request corresponding to the given map and returns the
   response map for corresponding to the resulting HTTP response.

   In addition to the standard Ring request keys, the following keys are also
   recognized:
   * :url
   * :method
   * :query-params"}
  request (wrap-request core/request))

(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :delete :url url})))

(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :get :url url})))

(defn head
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :head :url url})))

(defn move
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :move :url url})))

(defn options
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :options :url url})))

(defn patch
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :patch :url url})))

(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :post :url url})))

(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req]]
  (request (merge req {:method :put :url url})))
