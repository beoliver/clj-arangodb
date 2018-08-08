(ns clj-arangodb.arangodb.databases-test
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.core :as c]
            [clojure.test :refer :all]))

(defmacro with-db
  [[db label] & body]
  `(let [conn# (c/connect {:user "test"})
         ~db (c/db conn# ~label)]
     (when-not (d/exists? ~db)
       (c/create-database conn# ~label))
     ~@body
     (d/drop ~db)
     (c/shutdown conn#)))

(deftest collection-exists-test
  (with-db [db "some_database"]
    (let [label "some_collection"]
      (d/create-collection db label)
      (is (contains? (set (d/get-collection-names db)) label))
      (is (d/collection-exists? db label)))))
