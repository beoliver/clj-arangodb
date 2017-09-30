(ns clj-arangodb.velocypack.core-test
  (:require [clj-arangodb.velocypack.core :as v]
            [clojure.test :refer :all]))

(deftest can-compare-equality
  (let [data1 {:a "a" :b "b"}
        data2 {:a 1 :b 2}
        data3 {:a "a" :b 2}
        data4 {:a "a" :b 2 :d {:e 3 :f "g"}}]
    (is (= (v/pack data1) (v/pack data1)))
    (is (= (v/pack data2) (v/pack data2)))
    (is (= (v/pack data3) (v/pack data3)))
    (is (= (v/pack data4) (v/pack data4)))))
