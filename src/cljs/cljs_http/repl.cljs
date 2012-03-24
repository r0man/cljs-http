(ns cljs-http.repl
  (:require [clojure.browser.repl :as repl]))

(defn repl-connect []
  (repl/connect "http://localhost:9000/repl"))
