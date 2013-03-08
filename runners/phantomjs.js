#!/usr/bin/env phantomjs

// reusable phantomjs script for running clojurescript.test tests
// see http://github.com/cemerick/clojurescript.test for more info

var p = require('webpage').create();
p.injectJs(require('system').args[1]);

p.onConsoleMessage = function (x) { console.log(x); };
p.evaluate(function () {
  // can't just assign console.log directly?
  cljs.core._STAR_print_fn_STAR_ = function (x) {
                                       x = x.replace(/\n/g, "");
                                       console.log(x);
                                   };
});

var success = p.evaluate(function () {
  var results = cemerick.cljs.test.run_all_tests();
  cljs.core.println(results);
  return cemerick.cljs.test.successful_QMARK_(results);
});

phantom.exit(success ? 0 : 1);
