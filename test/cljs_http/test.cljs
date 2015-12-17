(ns cljs-http.test
  (:require [cljs-http.client-test]
            [cljs-http.core-test]
            [cljs-http.util-test]
            [doo.runner :refer-macros [doo-tests]]))

(doo-tests
 'cljs-http.client-test
 'cljs-http.core-test
 'cljs-http.util-test)
