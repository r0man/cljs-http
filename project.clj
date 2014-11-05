(defproject cljs-http "0.1.21-SNAPSHOT"
  :description "A ClojureScript HTTP library."
  :url "http://github.com/r0man/cljs-http"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[noencore "0.1.16"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.188"]]
  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.3"]]}}
  :plugins [[com.cemerick/clojurescript.test "0.3.0"]
            [lein-cljsbuild "1.0.3"]]
  :hooks [leiningen.cljsbuild]
  :min-lein-version "2.0.0"
  :deploy-repositories [["releases" :clojars]]
  :cljsbuild {:builds [{:compiler {:output-to "target/cljs-http-debug.js"}
                        :source-paths ["src"]}
                       {:compiler {:output-to "target/cljs-http.js"
                                   :optimizations :advanced
                                   :pretty-print false}
                        :source-paths ["src"]}
                       {:compiler {:output-to "target/cljs-http-test.js"
                                   :optimizations :advanced
                                   :pretty-print true}
                        :source-paths ["test"]}]
              :test-commands {"phantom" ["phantomjs" :runner "target/cljs-http-test.js"]}}
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
