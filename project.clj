(defproject cljs-http/cljs-http "0.0.2-SNAPSHOT"
  :description "A ClojureScript HTTP library."
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.2.1"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-path "src/cljs"
                        :compiler {:output-to "target/cljs-http-debug.js"}}
                       {:compiler {:output-to "target/cljs-http.js"
                                   :optimizations :advanced
                                   :pretty-print false}
                        :source-path "src/cljs"}
                       {:compiler {:output-to "target/cljs-http-test.js"
                                   :optimizations :advanced
                                   :pretty-print true}
                        :jar true
                        :source-path "test/cljs"}]
              :repl-listen-port 9000
              :repl-launch-commands
              {"chromium" ["chromium" "http://localhost:9000/"]
               "firefox" ["firefox" "http://localhost:9000/"]}
              :test-commands {"unit" ["./test-cljs.sh"]}}
  :source-path "src/clj")
