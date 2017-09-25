(ns clj-arangodb.arangodb.core-test
  (:require [clj-arangodb.arangodb.core :refer :all]
            [clojure.test :refer :all]))

(defn rand-uppercase-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(deftest get-databases-test
  (testing "can get databases")
  (is (vector? (get-databases (new-arrangodb {})))))

(deftest create-get-and-drop-database-test
  (testing "can create and drop a database"
    (let [conn (new-arrangodb {})
          db-name (rand-uppercase-str 10)]
      (is (= true (create-database conn db-name)))
      (is (get-database conn db-name))
      (is (= true (drop-database conn db-name))))))

(deftest multiple-threads-can-create-databases
  (testing "multiple threads can create databases")
  (let [conn (new-arrangodb {})
        db-count (count (get-databases conn))
        names (repeatedly 10 (fn [] (rand-uppercase-str 10)))]
    (is (= 10 (count names)))
    (doall (pmap (fn [name] (create-database conn name)) names))
    (is (= 10 (- (count (get-databases conn)) db-count)))
    (doall (pmap (fn [name] (drop-database conn name)) names))
    (is (= db-count (count (get-databases conn))))))
