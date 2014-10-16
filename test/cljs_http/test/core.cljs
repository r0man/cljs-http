(ns cljs-http.test.core
  (:require-macros [cemerick.cljs.test :refer [is deftest testing]])
  (:require [cemerick.cljs.test :as t]
            [cljs.core.async :as async]
            [cljs-http.core :as core]))

(deftest test-request
  (testing "it does not use the request's prepared channel"
    (let [prep-channel (async/chan)
          req {:uri "/foo/bar" :channel prep-channel}]
      (is (not= prep-channel (core/request req))))))
