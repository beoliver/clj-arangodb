# clj-arangodb
Clojure wrappers for the arangodb java client and velocypack libs

Early statges an will definitely change

```clojure
(ns my.project.core
  (:require [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.databases :as databases]
            [clj-arangodb.arangodb.collections :as collections]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.cursor :as cursor]))

(def conn (conn/new-arrangodb {}))

(def my-database
  (do
    (arango/create-database conn "myDatabase")
    (arango/get-database conn "myDatabase")))

(def my-collection
  (do
    (databases/create-collection my-database "myCollection")
    (databases/get-collection my-database "myCollection")))
    
(for [simpson [(vpack/vpack {:name "Homer" :age 38})
               (vpack/vpack {:name "Marge" :age 36})
               (vpack/vpack {:name "Bart" :age 10})
               (vpack/vpack {:name "Lisa" :age 8})
               (vpack/vpack {:name "Maggie" :age 2})]]
  (collections/insert-document my-collection simpson))
  
(let [query-fn (aql/stmt {"min" 9 "max" 37 "@coll" "myCollection"}
                         "FOR u IN @@coll FILTER (u.age > @min) AND (u.age < @max) RETURN u.name")]
  (cursor/as-list (query-fn db VPackSlice)))
```
