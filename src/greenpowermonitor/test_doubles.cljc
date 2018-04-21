(ns greenpowermonitor.test-doubles
  #?(:cljs (:require-macros
             [greenpowermonitor.test-doubles])))

(def ^:dynamic *spies-atom* (atom {}))

(defn make-spy-fn []
  (let [func-atom (atom [])]
    (letfn [(spy-fn [& args]
              (swap! func-atom conj (vec args)))]
      (swap! *spies-atom* conj {spy-fn func-atom})
      spy-fn)))

(defn make-mult-calls-stub-fn [values]
  (let [values-atom (atom values)]
    (fn [& _]
      (if (empty? @values-atom)
        (throw (ex-info "Too many calls to stub"
                        {:causes :calls-exceeded
                         :provided-return-values values}))
        (let [value (first @values-atom)]
          (swap! values-atom rest)
          value)))))

(defn make-stub-fn [key args]
  (case key
    :maps (fn [& fn-args]
            (let [call-args (vec fn-args)]
              (get args call-args (:any args))))
    :returns (make-mult-calls-stub-fn args)
    :constantly (constantly args)
    (throw (ex-info "make-stub-fn called with unknown keyword"
                    {:cause :unkown-keyword
                     :key key}))))

#?(:clj
   (defn- create-spying-list [functions]
     (mapcat #(vector % `(make-spy-fn)) functions)))

#?(:clj
   (defn- create-stubbing-list [functions]
     (->> functions
          (partition 3)
          (mapcat (fn [[func key values]] [func `(make-stub-fn ~key ~values)])))))

#?(:clj
   (defn- create-ignoring-list [functions]
     (mapcat #(vector % `(constantly nil)) functions)))

#?(:clj
   (defn- create-doubles-list [spying stubbing ignoring]
     (vec (concat (create-spying-list spying)
                  (create-stubbing-list stubbing)
                  (create-ignoring-list ignoring)))))

#?(:clj
   (defn- extract-with-double-args [args]
     (let [double-def? #(or (vector? %) (keyword? %))]
       (split-with double-def? args))))

#?(:clj
   (defmacro with-doubles [& args]
     (let [[doubles body] (extract-with-double-args args)
           {:keys [spying stubbing ignoring]
            :or {spying [] stubbing [] ignoring []}} doubles]
       `(with-redefs ~(create-doubles-list spying stubbing ignoring)
          ~@body
          (reset! *spies-atom* {})))))

(defn calls-to [function]
  (if-let [calls (some-> *spies-atom* deref (get function) deref)]
    calls
    (let [error-msg "Attempting to check calls for a function that is not being spied on"]
      (throw #?(:clj  (Exception. error-msg)
                :cljs (js/Error. error-msg))))))
