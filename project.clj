(defproject cljs-http "0.1.44-SNAPSHOT"
  :description "A ClojureScript HTTP library."
  :url "https://github.com/r0man/cljs-http"
  :license {:name "Eclipse Public License"
            :url "https://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[noencore "0.3.3"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229" :scope "provided"]
                 [org.clojure/core.async "0.3.442"]
                 [com.cognitect/transit-cljs "0.8.239"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-doo "0.1.7"]]
  :aliases {"test" ["do"
                    ["clean"]
                    ["doo" "phantom" "none" "once"]
                    ["doo" "phantom" "advanced" "once"]]}
  :cljsbuild {:builds
              [{:id "none"
                :compiler
                {:asset-path "target/none"
                 :main cljs-http.test
                 :optimizations :none
                 :output-dir "target/none"
                 :output-to "target/none.js"}
                :source-paths ["src" "test"]}
               {:id "advanced"
                :compiler
                {:asset-path "target/advanced"
                 :main cljs-http.test
                 :optimizations :advanced
                 :output-dir "target/advanced"
                 :output-to "target/advanced.js"
                 :pretty-print true}
                :source-paths ["src" "test"]}]}
  :deploy-repositories [["releases" :clojars]]
  :min-lein-version "2.0.0"
  :profiles {:dev {:dependencies [[doo "0.1.7"]]}})
