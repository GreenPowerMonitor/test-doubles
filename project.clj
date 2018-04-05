(defproject greenpowermonitor/test-doubles "0.1.1-SNAPSHOT"
  :description "A small spying and stubbing library for Clojure and ClojureScript"
  :url "https://github.com/GreenPowerMonitor/test-doubles"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :scm {:name "git"
        :url "https://github.com/GreenPowerMonitor/test-doubles"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]
            [lein-auto "0.1.3"]]

  :auto {"test" {:file-pattern #"\.(clj|cljs|cljc|edn)$"}}

  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]

  :cljsbuild {:builds [{:id "unit-tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "out/unit_tests.js"
                                   :main greenpowermonitor.unit-tests-runner
                                   :target :nodejs
                                   :optimizations :none}}]})
