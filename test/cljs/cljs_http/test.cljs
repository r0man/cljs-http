(ns cljs-http.test
  (:require [cljs-http.test.core :as core]
            [cljs-http.test.util :as util]))

(defn ^:export run []
  (core/test)
  (util/test)
  "All tests passed.")
