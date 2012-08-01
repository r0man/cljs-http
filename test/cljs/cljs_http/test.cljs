(ns cljs-http.test
  (:require [cljs-http.test.core :as core]
            [cljs-http.test.client :as client]
            [cljs-http.test.util :as util]))

(defn ^:export run []
  (client/test)
  (core/test)
  (util/test)
  "All tests passed.")
