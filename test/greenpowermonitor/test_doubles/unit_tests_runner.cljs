(ns greenpowermonitor.test-doubles.unit-tests-runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [greenpowermonitor.test-doubles.core-test]))

(doo-tests
  'greenpowermonitor.test-doubles.core-test)
