(defproject cljs-http "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :dev-dependencies [[lein-cljsbuild "0.0.11"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild [{:source-path "src/cljs"
               :jar true
               :compiler {:output-to "cljs-http.js"
                          ;; :optimizations :advanced
                          :optimizations :whitespace
                          :pretty-print true}}])