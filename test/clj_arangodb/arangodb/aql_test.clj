(ns clj-arangodb.arangodb.aql-test
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.test.helper :as h]
            [clj-arangodb.arangodb.cursor :as cursor]
            [clj-arangodb.arangodb.adapter :as adapter]
            [clojure.test :refer :all]))

(deftest let-test-1
  (h/with-temp-db [db "testDB"]
    (let [qry [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
                     "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
                     "z" 100]
               [:RETURN [:SUM ["x" "y" "z"]]]]]
      (is (= 158 (cursor/first (d/query db qry Integer)))))))

(deftest let-test-2
  (h/with-temp-db [db "testDB"]
    (let [qry [:LET ["x" [:SUM [1 2 3 4 5 6 7 8 9 10]]
                     "y" [:LENGTH ["\"a\"" "\"b\"" "\"c\""]]
                     "z" [1 2 3 4 5]]
               [:RETURN [:SUM ["x" "y" [:SUM "z"]]]]]]
      (is (= 73 (cursor/first (d/query db qry Integer)))))))

(deftest can-encode-nested-funs
  (is (= "sum([sum([1,2,3]),sum([2,3,4])])"
         (aql/serialize [:sum [[:sum [1 2 3]] [:sum [2 3 4]]]])))
  (h/with-temp-db [db "testDB"]
    (let [query [:return [:sum [[:sum [1 2 3]] [:sum [2 3 4]]]]]]
      (is (= 15 (cursor/first (d/query db query Integer)))))))

(deftest obj-test
  (h/with-temp-db [db "testDB"]
    (let [query-1 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {"a" [:SUM [1 2 3]] "b" "b"}]]
          query-2 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {:a [:SUM [1 2 3]] :b "b"}]]]
      (is (= (->> query-1
                  (d/query db)
                  cursor/first
                  adapter/deserialize-doc)
             (->> query-2
                  (d/query db)
                  cursor/first
                  adapter/deserialize-doc)
             {:a 6.0 :b 6.0})))))
