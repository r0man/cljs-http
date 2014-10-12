# cljs-http
  [![Build Status](https://travis-ci.org/r0man/cljs-http.png)](https://travis-ci.org/r0man/cljs-http)
  [![Dependencies Status](http://jarkeeper.com/r0man/cljs-http/status.png)](http://jarkeeper.com/r0man/cljs-http)
  [![Gittip](http://img.shields.io/gittip/r0man.svg)](https://www.gittip.com/r0man)

A ClojureScript HTTP library.

![](http://imgs.xkcd.com/comics/server_attention_span.png)

## Installation

Via Clojars: http://clojars.org/cljs-http

[![Current Version](https://clojars.org/cljs-http/latest-version.svg)](https://clojars.org/cljs-http)

## Usage

```clojure
(ns example.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(go (let [response (<! (http/get "https://api.github.com/users" {:with-credentials? false}))]
      (prn (:status response))
      (prn (map :login (:body response)))))

;; POSTing automatically sets Content-Type header and serializes
(http/post "http://example.com" {:edn-params {:foo :bar}})

;; JSON is auto-converted via `cljs.core/clj->js`
(http/post "http://example.com" {:json-params {:foo :bar}})

;; Form parameters in a POST request (simple)
(http/post "http://example.com" {:form-params {:key1 "value1" :key2 "value2"}})

;; Form parameters in a POST request (array of values)
(http/post "http://example.com" {:form-params {:key1 [1 2 3] :key2 "value2"}})

;; HTTP Basic Authentication
(http/get
  "http://example.com"
  {:basic-auth {:username "hello" :password "world"}})

;; Pass prepared channel that will be returned,
;; e.g. to use a transducer.
(http/get "http://example.com" {:channel (chan 1 (map :body))})
```

## License

Copyright (C) 2012-2014 r0man

Distributed under the Eclipse Public License, the same as Clojure.
