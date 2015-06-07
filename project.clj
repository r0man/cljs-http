(defproject cljs-http "0.1.35"
  :description "A ClojureScript HTTP library."
  :url "https://github.com/r0man/cljs-http"
  :license {:name "Eclipse Public License"
            :url "https://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[noencore "0.1.19"]
                 [org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3165" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.220"]]
  :plugins [[com.cemerick/clojurescript.test "0.3.3"]
            [lein-cljsbuild "1.0.6"]]
  :aliases {"test" ["do" "clean," "cljsbuild" "test"]
            "test-ancient" ["do" "clean," "cljsbuild" "test"]}
  :min-lein-version "2.0.0"
  :deploy-repositories [["releases" :clojars]]
  :cljsbuild {:builds [{:compiler {:output-to "target/testable.js"
                                   :optimizations :advanced
                                   :pretty-print true}
                        :source-paths ["test"]}]
              :test-commands {"phantom" ["phantomjs" :runner "target/testable.js"]}})
