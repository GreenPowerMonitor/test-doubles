(ns greenpowermonitor.test-doubles-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [greenpowermonitor.test-doubles :as td]))

(defn some-function [a b]
  (println a b))

(defn greetings-function []
  (println "Hola!"))

(defn do-some-computation-fn [_ _])

(deftest spying-functions
  (td/with-doubles
    :spying [some-function
             println]

    (some-function "koko" "triki")
    (some-function "miko" "miki")

    (greetings-function)
    (greetings-function)
    (greetings-function)

    (is (= 2 (-> some-function td/calls-to count)))
    (is (= [["koko" "triki"] ["miko" "miki"]] (td/calls-to some-function)))
    (is (= ["koko" "triki"] (-> some-function td/calls-to first)))

    (is (= 3 (-> println td/calls-to count)))
    (is (->> println td/calls-to (every? #(= % ["Hola!"]))))

    (try
      (td/calls-to greetings-function)
      (catch #?(:clj  Exception
                :cljs :default)
             e
        (is (= "Attempting to check calls for a function that is not being spied on"
               #?(:clj  (.getMessage e)
                  :cljs (ex-message e))))))))

(deftest ignoring-functions
  (let [double-and-greet (fn [x] (print x) (greetings-function) (* 2 x))]

    (is (= "2Hola!\n" (with-out-str (double-and-greet 2))))

    (td/with-doubles
      :ignoring [greetings-function
                 print]

      (is (= "" (with-out-str (double-and-greet 2)))))))

(deftest stubbing-functions
  (testing "make a function return a given sequence of values in successive calls"
    (td/with-doubles
      :stubbing [rand-nth :returns [1 4 6]]

      (is (= 1 (rand-nth [])))
      (is (= 4 (rand-nth [2 3])))
      (is (= 6 (rand-nth [1 2 3])))

      #?(:cljs (is (thrown? js/Error (rand-nth [9])))
         :clj  (is (thrown? Exception (rand-nth [5 8]))))

      (try
        (rand-nth [])
        (catch #?(:clj  Exception
                  :cljs :default)
               e
          (is (= "Too many calls to stub"
                 #?(:clj  (.getMessage e)
                    :cljs (ex-message e))))
          (is (= {:causes :calls-exceeded
                  :provided-return-values [1 4 6]}
                 (ex-data e)))))))

  (testing "make a function return always the same value"
    (td/with-doubles
      :stubbing [rand :constantly 1]

      (is (= 1 (rand)))
      (is (= 1 (rand)))
      (is (= 1 (rand)))
      (is (= 1 (rand)))))

  (testing "make a function return specific outputs for specific inputs"
    (td/with-doubles
      :stubbing [do-some-computation-fn :maps {[1 2] ::output-for-args-1-and-2
                                               [2 3] ::output-for-args-2-and-3
                                               :any ::output-for-any-other-arguments}
                 constantly :maps {[1] 30
                                   [2] 40}]

      (is (= ::output-for-args-1-and-2 (do-some-computation-fn 1 2)))
      (is (= ::output-for-args-2-and-3 (do-some-computation-fn 2 3)))
      (is (= ::output-for-any-other-arguments (do-some-computation-fn 5 6)))

      (is (= 30 (constantly 1)))
      (is (= 40 (constantly 2)))
      (is (= nil (constantly 6)))))

  (testing "throws an exception when stubbing with an unknown option"
    (try
      (td/with-doubles
        :stubbing [constantly :some-unknown-option 3]

        #_any-code)
      (catch #?(:clj  Exception
                :cljs :default)
             e
        (is (= "Using :stubbing with an unknown option"
               #?(:clj  (.getMessage e)
                  :cljs (ex-message e))))
        (is (= {:cause :unknown-option
                :used-option :some-unknown-option
                :available-options [:maps :returns :constantly]}
               (ex-data e)))))))
