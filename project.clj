(defproject cljs-http "0.0.1-SNAPSHOT"
  :description "A ClojureScript HTTP library."
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :dev-dependencies [[lein-cljsbuild "0.0.11"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild [{:source-path "src"
               :compiler {:output-to "cljs-http.js"
                          :optimizations :advanced}}])