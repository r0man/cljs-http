# cljs-http [![Build Status](https://travis-ci.org/r0man/cljs-http.png)](https://travis-ci.org/r0man/cljs-http)

A ClojureScript HTTP library.

## Installation

Via Clojars: http://clojars.org/cljs-http

## Usage

    (ns example.core
      (:require-macros [cljs.core.async.macros :refer [go]])
      (:require [cljs-http.client :as http]
                [cljs.core.async :refer [<!]]))

    (go (let [response (<! (http/get "https://api.github.com/users" {:with-credentials? false}))]
          (prn (:status response))
          (prn (map :login (:body response)))))

## License

Copyright (C) 2012-2013 Roman Scherer

Distributed under the Eclipse Public License, the same as Clojure.
