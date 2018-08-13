(ns clj-arangodb.arangodb.cursor-test
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clj-arangodb.arangodb.cursor :as cursor]
            [clj-arangodb.arangodb.test.helper :as h]
            [clojure.test :refer :all]))

(deftest cursor-stats-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:RETURN true])
          stats (cursor/get-stats res)]
      (is (= (:class stats)
             com.arangodb.entity.CursorEntity$Stats))
      (is (= (set (keys stats))
             #{:class :executionTime :filtered :fullCount
               :scannedFull :scannedIndex :writesExecuted
               :writesIgnored})))))

(deftest cursor-count-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil {:count true} Integer)]
      (is (= 5 (cursor/get-count res))))
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]])]
      (is (= 5 (cursor/count res))))))

(deftest cursor-cache-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]])]
      (is (= false (cursor/is-cached res))))))

(deftest cursor-collect-into-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs  (cursor/collect-into res (java.util.ArrayList.))]
      (is (= [1 2 3 4 5] xs))
      (testing "collect-into consumes the contents of the cursor"
        (is (= false (cursor/has-next res)))))
    (let [res (d/query db [:FOR ["x" [1 1 2 2 3]] [:RETURN "x"]] nil nil Integer)
          xs  (cursor/collect-into res (java.util.HashSet.))]
      (is (= #{1 2 3} xs)))))

(deftest cursor-seq-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs  (seq res)]
      (is (= (type xs) clojure.lang.LazySeq))
      (is (= [1 2 3 4 5] xs)))
    (testing "can map deserialize-doc"
      (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]])
            xs  (map adapter/deserialize-doc res)]
        (is (= (type xs) clojure.lang.LazySeq))
        (is (= [1 2 3 4 5] xs))))))

(deftest cursor-predicate-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)]
      (is (= true (cursor/all-match res (partial >= 5))))
      (testing "can only test once"
        (is (= false (cursor/all-match res nat-int?)))))))

(deftest cursor-map-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs (cursor/map res inc)]
      (is (= [1 2 3 4 5] (seq res)))
      (is (= nil (seq xs))))
    (let [res (d/query db [:FOR ["x" "1..5"] [:RETURN "x"]] nil nil Integer)
          xs (cursor/map res inc)]
      (is (= [2 3 4 5 6] (seq xs)))
      (is (= (seq res) nil)))    ))

(deftest cursor-first-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..3"] [:RETURN "x"]] nil nil Integer)]
      (is (= 1 (cursor/first res)))
      (is (= 2 (cursor/first res)))
      (is (= 3 (cursor/first res)))
      (is (nil? (cursor/first res))))))

(deftest cursor-foreach-test
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db [:FOR ["x" "1..3"] [:RETURN "x"]] nil nil Integer)
          state (atom 0)]
      (cursor/foreach res (fn [x] (swap! state + x)))
      (is (nil? (cursor/first res)))
      (is (= @state 6)))))

(deftest multi-batch-tests
  (h/with-temp-db [db "testDB"]
    (let [res (d/query db
                       [:FOR ["x" "0..99"] [:RETURN "x"]]
                       nil
                       {:batchSize (int 5)}
                       Integer)]
      (is (= (range 100) (seq res))))
    (let [res (d/query db
                       [:FOR ["x" "1..3"] [:RETURN "x"]]
                       nil
                       {:batchSize (int 1)}
                       Integer)]
      (while (cursor/has-next res)
        (is (nat-int? (cursor/next res)))))))
