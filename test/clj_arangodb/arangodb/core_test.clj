(ns clj-arangodb.arangodb.core-test
  (:require [clj-arangodb.arangodb.core :refer :all]
            [clojure.test :refer :all]))

(defn rand-uppercase-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(deftest create-get-and-drop-database-test
  (testing "can create and drop a database"
    (let [conn (new-arrangodb {})
          db-name (rand-uppercase-str 10)]
      (is (= true (create-database conn db-name)))
      (is (get-database conn db-name))
      (is (= true (drop-database conn db-name))))))
