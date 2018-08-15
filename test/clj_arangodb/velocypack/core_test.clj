(ns clj-arangodb.velocypack.core-test
  (:require [clj-arangodb.velocypack.core :as v]
            [clojure.test :refer :all]))

(deftest pack-unpack-test
  (let [id (comp v/unpack v/pack)]
    (doseq [datum [1
                   8172
                   1872.283
                   true
                   false
                   "hello world"
                   [1 "foo" 2 "bar" {:a "a" :b 2}]
                   {:a "a" :b "b"}
                   {:a 1 :b 2}
                   {:a "a" :b 2}
                   {1 "one" 2 "two" 3.0 "three"}
                   {:a "a" :b 2 :d {:e 3 :f "g"}}]]
      (is (= (v/pack datum) (v/pack datum)))
      (is (= datum (id datum))))))
