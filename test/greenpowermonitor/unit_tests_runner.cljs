(ns greenpowermonitor.unit-tests-runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [greenpowermonitor.test-doubles-test]))

(doo-tests
  'greenpowermonitor.test-doubles-test)
