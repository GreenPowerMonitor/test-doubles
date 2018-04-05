(ns gpm.test-doubles.unit-tests-runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [gpm.test-doubles.core-test]))

(doo-tests
  'gpm.test-doubles.core-test)
