# cljs-http
  [![Build Status](https://travis-ci.org/r0man/cljs-http.svg)](https://travis-ci.org/r0man/cljs-http)
  [![Dependencies Status](https://jarkeeper.com/r0man/cljs-http/status.svg)](http://jarkeeper.com/r0man/cljs-http)

A ClojureScript HTTP library.

![](http://imgs.xkcd.com/comics/server_attention_span.png)

## Installation

Via Clojars: http://clojars.org/cljs-http

[![Current Version](https://clojars.org/cljs-http/latest-version.svg)](https://clojars.org/cljs-http)

## Usage

#### Require:

```clojure
(ns example.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))
```

### Async response handling:
The http functions will return a channel:
```clojure
(go (let [response (<! (http/get "https://api.github.com/users" {:with-credentials? false
                                                                 :query-params {"since" 135}}))]
      (prn (:status response))
      (prn (map :login (:body response)))))

```

You can pass a channel for the result to be written on.
This is usefull when using a transducer:
```clojure
(http/get "http://example.com" {:channel (chan 1 (map :body))})
```

### Post

POSTing automatically sets Content-Type header and serializes

You can send the body params in mutiple formats:

```clojure
(http/post "http://example.com" {:edn-params {:foo :bar}})

(http/post "http://example.com" {:json-params {:foo :bar}})
;;(JSON is auto-converted via `cljs.core/clj->js`)

(http/post "http://example.com" {:transit-params {:key1 "value1" :key2 "value2"}})

(http/post "http://example.com" {:form-params {:key1 "value1" :key2 "value2"}})
```


**To send form parameters (an array of values):**
```clojure
(http/post "http://example.com" {:form-params {:key1 [1 2 3] :key2 "value2"}})
```


**To upload a  file using Multipart parameters:**
```clojure
(http/post "http://example.com" {:multipart-params [["key1" "value1"] ["my-file" my-file]])
```
Where `my-file` can be one of:
- a Blob instance, eg:
     ```clojure
     (let [my-file (js/Blob. #js ["<h1>Hello</h1>"] #js {:type "text/html})] ...)
     ```
     
- a File instance, eg:
    ```clojure
     HTML: <input id="my-file" type="file">
     (let [my-file (-> (.getElementById js/document "my-file")
                       .-files first)]
                       ...)
     ```

If you want to set the name of the file in the request
(https://developer.mozilla.org/en-US/docs/Web/API/FormData/append#Syntax),
simply set my-file to be a vector: `["myfile" [value filename]]`.

### HTTP Basic Authentication
```clojure
(http/get
  "http://example.com"
  {:basic-auth {:username "hello" :password "world"}})
```

### Upload/Download progress monitoring
```clojure
(let [progress-channel (async/chan)]
  (http/post "http://example.com" {:multipart-params [["key1" "value1"] ["my-file" my-file]] :progress progress-chan}))
```
The progress-channel will receive progress events: {:directon dir :loaded uploaded_or_downloaded :total size}
- :direction is :upload or :download
- in some cases :total can be missing

  
### JSONP
```clojure
(http/jsonp "http://example.com" {:callback-name "callback" :timeout 3000})
```
Where `callback-name` is used to specify JSONP callback param name. Defaults to "callback".
`timeout` is the length of time, in milliseconds.
This channel is prepared to wait for for a request to complete.
If the call is not competed within the set time span, it is assumed to have failed.


## License

Copyright (C) 2012-2014 r0man

Distributed under the Eclipse Public License, the same as Clojure.
