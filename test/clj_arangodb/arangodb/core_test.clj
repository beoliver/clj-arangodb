(ns clj-arangodb.arangodb.core-test
  (:require [clj-arangodb.arangodb.core :refer :all]
            [clj-arangodb.arangodb.databases :as dbs]
            [clj-arangodb.arangodb.collections :as coll]
            [clojure.test :refer :all]))

(defn rand-db-name []
  (str "testdb"
       (apply str (take 10 (repeatedly #(char (+ (rand 26) 65)))))))

(defn rand-coll-name []
  (str "testcoll"
       (apply str (take 10 (repeatedly #(char (+ (rand 26) 65)))))))

(deftest get-databases-test
  (testing "can get databases")
  (is (vector? (get-databases (new-arrangodb {})))))

(deftest create-get-and-drop-database-test
  (testing "can create and drop a database"
    (let [conn (new-arrangodb {})
          db-name (rand-db-name)]
      (is (= true (create-database conn db-name)))
      (is (get-database conn db-name))
      (is (= true (drop-database conn db-name))))))

(deftest multiple-threads-can-create-databases
  (testing "multiple threads can create databases")
  (let [conn (new-arrangodb {})
        db-count (count (get-databases conn))
        names (repeatedly 10 (fn [] (rand-db-name)))]
    (is (= 10 (count names)))
    (doall (pmap (fn [name] (create-database conn name)) names))
    (is (= 10 (- (count (get-databases conn)) db-count)))
    (doall (pmap (fn [name] (drop-database conn name)) names))
    (is (= db-count (count (get-databases conn))))))

(deftest create-get-and-drop-collection-test
  (testing "can create and drop a collection"
    (let [conn (new-arrangodb {})
          db-name (rand-db-name)
          coll-name (rand-coll-name)]
      (create-database conn db-name)
      (let [db (get-database conn db-name)]
        (is (dbs/create-collection db coll-name))))))
