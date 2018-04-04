(defproject gpm/test-doubles "0.1.0"
  :description "A small spying and stubbing library for Clojure and ClojureScript"
  :url "https://github.com/GreenPowerMonitor/test-doubles"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]
            [lein-auto "0.1.3"]]

  :auto {"test" {:file-pattern #"\.(clj|cljs|cljc|edn)$"}}

  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]

  :cljsbuild {:builds [{:id "min"
                        :source-paths ["src"]
                        :compiler {:main gpm.test-doubles.core
                                   :output-to "resources/deploy/js/compiled/app.js"
                                   :optimizations :advanced
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print false}}

                       {:id "unit-tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "out/unit_tests.js"
                                   :main gpm.test-doubles.unit-tests-runner
                                   :target :nodejs
                                   :optimizations :none}}]})
