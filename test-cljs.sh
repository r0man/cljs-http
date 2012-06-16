#!/usr/bin/env bash
if [ ! -f target/cljs-http-test.js ]; then
    lein cljsbuild once
fi
echo "cljs_http.test.run()" | d8 --shell target/cljs-http-test.js
