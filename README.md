# test-doubles

A small spying and stubbing library for Clojure and ClojureScript.

## Install

Add `[gpm/test-doubles "0.1.0"]` to `[:profiles :dev :dependencies]` in your `project.clj`.

## Usage

`with-doubles` takes a function symbol and replaces it in the scope of its body.

Be aware that `with-doubles` only works with functions. It won't work with macros or special forms.  [<sup>1</sup>](#nota1)

### 1. Stubbing function calls
You can use the `:stubbing` option inside the `with-doubles` macro to **stub calls** to any function included in the vector that appears right after the `:stubbing` keyword.

There are three different ways of stubbing a function.

#### 1.1. `:returns` option
This option makes a function **return a given sequence of values in successive calls**.

In the following example[<sup>2</sup>](#nota2), we stub the `rand` function to make it return four values in sequence, `1`, `4`, `6`, `3`. When you call `rand` more times than the number of values in the provided sequence, an exception gets thrown (see the message and data in the exception at the end of this test).

```clojure
(ns gpm.test-doubles.stubbing-with-returns-examples
  (:require
   [clojure.test :refer [deftest testing is]]
   [gpm.test-doubles.core :as td]))

(deftest stubbing-functions-using-returns
  (testing "make a function return a given sequence of values in successive calls"
    (td/with-doubles
      :stubbing [rand :returns [1 4 6 3]]

      (is (= 1 (rand)))
      (is (= 4 (rand)))
      (is (= 6 (rand)))
      (is (= 3 (rand)))

      (is (thrown? js/Error (rand)))

      (try
        (rand)
        (catch :default e
          (is (= "Too many calls to stub" (ex-message e)))
          (is (= {:causes :calls-exceeded, :provided-return-values [1 4 6 3]}
                 (ex-data e))))))))
```
#### 1.2. `:constantly`
This option makes a function **always return the same value**.

In the following example, we stub the `rand` function so that it always returns 1.
```clojure
(ns gpm.test-doubles.stubbing-with-constantly-examples
  (:require
   [clojure.test :refer [deftest testing is]]
   [gpm.test-doubles.core :as td]))

(deftest stubbing-functions-using-constantly
  (testing "make a function return always the same value"
    (td/with-doubles
      :stubbing [rand :constantly 1]

      (is (= 1 (rand)))
      (is (= 1 (rand)))
      (is (= 1 (rand)))
      (is (= 1 (rand))))))
```

#### 1.3. `:maps`
This option makes a function **return specific outputs for specific inputs**.

In the following example, we stub the `do-some-computation-fn` function so that it returns:

  - `::output-for-args-1-and-2` when called with the arguments `1` and `2`.
  - `::output-for-args-2-and-3` when called with the arguments `2` and `3`.
  - `::output-for-any-other-arguments` when called with any other arguments.

and the `constantly` function so that it returns:

  - `30` when called with the argument `1`
  - `40` when called with the argument `2`

Notice how, in this last case, if you don't explicitly provide a value for any other arguments (using the keyword `:any`),
a `nil` will be returned when the received parameters don't match any of the map keys.

```clojure
(ns gpm.test-doubles.stubbing-with-maps-examples
  (:require
   [clojure.test :refer [deftest testing is]]
   [gpm.test-doubles.core :as td]))

  (defn do-some-computation-fn [_ _])

  (deftest stubbing-functions-using-maps
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
        (is (= nil (constantly 6))))))
```

### 2. Spying function calls
You can use the `:spying` option inside `with-doubles` macro to spy on all the calls to the functions included in the vector after `:spying` keyword. To check the calls to the spied functions and the arguments passed in each call, you have to use the `calls-to` function.

In the following example, we spy the calls to `some-function` and `println` functions. Then we call twice `some-function` and three times `greetings-function` (which calls `println`). Finally, we use `calls-to` function to check
the calls to each spied function and the arguments passed to them in each call.
```clojure
(ns gpm.test-doubles.spying-examples
  (:require
   [clojure.test :refer [deftest testing is]]
   [gpm.test-doubles.core :as td]))

(defn- some-function [a b]
  (println a b))

(defn- greetings-function []
  (println "Hola!"))

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
    (is (->> greetings-function td/calls-to (every? #(= % "Hola!"))))))
```
### 3. Ignoring function calls
You can use the `:ignoring` option inside `with-doubles` macro to ignore all the calls to the functions included in the vector after `:ignoring` keyword.

In the following example, even though you call the `double-print-x-and-greet` function inside `with-doubles`, nothing gets printed on the console because `greetings-function`and `print` get ignored.
```clojure
(ns gpm.test-doubles.stubbing-with-returns-examples
  (:require
   [clojure.test :refer [deftest testing is]]
   [gpm.test-doubles.core :as td]))

(defn- greetings-function []
  (println "Hola!"))

(deftest ignoring-functions
  (let [double-and-greet (fn [x] (print x) (greetings-function) (* 2 x))]

    (is (= "2Hola!\n" (with-out-str (double-and-greet 2))))

    (td/with-doubles
      :ignoring [greetings-function
                 print]

      (is (= "" (with-out-str (double-and-greet 2)))))))
```
### 4. Combining different types of test doubles inside `with-doubles` macro.
You can use as many test doubles as you want inside `with-doubles` macro.

In the following example, we are using two **stubs** (one with `:maps` option and another with `:returns` option) and a **spy**.
```clojure
(deftest saving-changes
  (let [changes {:interventions {53 7}}
         :expected-output :some-expected-request-data-to-save
         some-api-url "some-url"
         state-id 1]
    (td/with-doubles
      :stubbing [l/view :maps {[domain.rim/rim-wo-edit-changes-lens] changes
                               [domain.rim/rim-wo-edit-state-id-lens] state-id
                               [domain.rim/rim-wo-translated-lens] :translated-data-to-save}
                 c/mk-work-orders-save-url :returns [some-api-url]]
      :spying [service/put]

      (sut/save-changes!)

      (is (= 1 (-> service/put td/calls-to count)))
      (let [[url data] (-> service/put td/calls-to first)]
        (is (= some-api-url url))
        (is (= expected-output (:json-params data)))))))
```


## Footnotes

<a name="nota1"></a> 1. Notice that some verbs that are functions in Clojure might be macros in ClojureScript or viceversa. For instance `+` is a function in Clojure, but a macro in ClojureScript.

<a name="nota2"></a> 2. All the examples in this document should work in both Clojure and ClojureScript. We take advantage of _implicit macro loading_ and _auto-aliasing clojure namespaces_, read more about it in the [Differences from Clojure](https://clojurescript.org/about/differences) section of the ClojureScript site.


## License

Copyright © 2018 GreenPowerMonitor

Distributed under the Eclipse Public License version 1.0.
