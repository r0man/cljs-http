(defproject cljs-http/cljs-http "0.0.1-SNAPSHOT"
  :description "A ClojureScript HTTP library."
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :cljsbuild {:builds [{:source-path "src/cljs"
                        :compiler {:output-to "classes/cljs-http.js"
                                   :optimizations :advanced
                                   :pretty-print false}}
                       {:source-path "src/cljs"
                        :compiler {:output-to "classes/cljs-http-dev.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]
              :repl-listen-port 9000
              :repl-launch-commands
              {"chromium" ["chromium" "test-resources/index.html"]
               "firefox" ["firefox" "test-resources/index.html"]}}
  :plugins [[lein-cljsbuild "0.1.2"]]
  :hooks [leiningen.cljsbuild]
  :source-path "src/clj")
