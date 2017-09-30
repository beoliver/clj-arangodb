(ns clj-arangodb.arangodb.core-test
  (:require [clj-arangodb.arangodb.core :refer :all]
            [clj-arangodb.velocypack.core :as v]
            [clojure.test :refer :all]))

(defn rand-db-name []
  (str "testdb"
       (apply str (take 10 (repeatedly #(char (+ (rand 26) 65)))))))

(defn rand-coll-name []
  (str "testcoll"
       (apply str (take 10 (repeatedly #(char (+ (rand 26) 65)))))))

(deftest get-databases-test
  (testing "can get databases")
  (is (vector? (get-dbs (init-arangodb {})))))

(deftest create-get-and-drop-database-test
  (testing "can create and drop a database"
    (let [conn (init-arangodb {})
          db-name (rand-db-name)]
      (is (= true (create-db conn db-name)))
      (is (get-db conn db-name))
      (is (= true (drop-db conn db-name))))))

(deftest multiple-threads-can-create-databases
  (testing "multiple threads can create databases")
  (let [conn (init-arangodb {})
        db-count (count (get-dbs conn))
        names (repeatedly 10 (fn [] (rand-db-name)))]
    (is (= 10 (count names)))
    (doall (pmap (fn [name] (create-db conn name)) names))
    (is (= 10 (- (count (get-dbs conn)) db-count)))
    (doall (pmap (fn [name] (drop-db conn name)) names))
    (is (= db-count (count (get-dbs conn))))))

(deftest create-get-and-drop-collection-test
  (testing "can create and drop a collection"
    (let [conn (init-arangodb {})
          db-name (rand-db-name)
          coll-name (rand-coll-name)]
      (create-db conn db-name)
      (let [db (get-db conn db-name)]
        (is (create-coll db coll-name))))))

(deftest can-insert-into-collection
  (let [conn (init-arangodb {})
        db-name (rand-db-name)
        coll-name (rand-coll-name)]
    (create-db conn db-name)
    (create-coll conn db-name coll-name)
    (testing "can insert single entity"
      (let [coll (get-coll conn db-name coll-name)
            clojure-data {:name "Homer" :age 38 :neg -38}
            data (insert-doc coll (v/pack clojure-data))]
        (is (some? (:_id data)))
        (is (some? (:_key data)))
        (let [data' (get-vpack-doc-by-key coll (:_key data))
              data'' (get-vpack-doc-by-id conn db-name (:_id data))]
          (is (v/vpack-slice? data'))
          (is (v/vpack-slice? data''))
          (is (= (v/get* data' :_id) (:_id data)))
          (is (= (v/get* data' :name) (:name clojure-data)))
          (is (= (v/get* data' :age) (:age clojure-data)))
          (is (= (v/get* data' :neg) (:neg clojure-data)))
          (is (= (v/get* data'' :_key) (:_key data))))))
    (testing "can insert multiple entity"
      (let [coll (get-coll conn db-name coll-name)
            data (insert-docs coll (map v/pack [{:name "Marge" :age 36}
                                                {:name "Bart" :age 10}
                                                {:name "Lisa" :age 8}
                                                {:name "Maggie" :age 2}]))]
        (is (every? :_id data))
        (is (every? :_key data))))))
