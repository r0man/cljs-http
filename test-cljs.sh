#!/usr/bin/env bash

JS_TESTS="target/cljs-http-test.js"

[ ! -f $JS_TESTS ] && lein cljsbuild once

if [ -n "`type d8 2> /dev/null`" ] ; then
    echo "cljs_http.test.run()" | d8 --shell $JS_TESTS
elif [ -n "`type v8 2> /dev/null`" ] ; then
    echo "cljs_http.test.run()" | v8 --shell $JS_TESTS
else
    echo "Can't run ClojureScript tests. Looks like V8 is not installed."
    exit 1
fi
