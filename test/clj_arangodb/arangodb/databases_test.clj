(ns clj-arangodb.arangodb.databases-test
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.core :as c]
            [clojure.test :refer :all]
            [clj-arangodb.arangodb.helper :as h]))

(deftest collection-exists-test
  (h/with-temp-db [db "some_database"]
    (let [label "some_collection"]
      (d/create-collection db label)
      (is (contains? (set (d/get-collection-names db)) label))
      (is (d/collection-exists? db label)))))
