(ns clj-arangodb.arangodb.aql-test
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.helper :as h]
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
      (testing "default deserialization returns a float"
        (let [res (-> db
                      (d/query query)
                      cursor/first
                      adapter/deserialize-doc)]
          (is (= 15.0 res))))
      (testing "passing an Integer class returns an integer"
        (let [res (-> db
                      (d/query query Integer)
                      cursor/first)]
          (is (= 15 res)))))))

(deftest obj-test
  (h/with-temp-db [db "testDB"]
    (let [query-1 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {"a" [:SUM [1 2 3]] "b" "b"}]]
          query-2 [:LET ["a" [:SUM [1 2 3]]
                         "b" "a"]
                   [:RETURN {:a [:SUM [1 2 3]] :b "b"}]]]
      (is (= (-> (d/query db query-1)
                 cursor/first
                 adapter/deserialize-doc)
             (-> (d/query db query-2)
                 cursor/first
                 adapter/deserialize-doc)
             {:a 6.0 :b 6.0})))))
