# clj-arangodb
Clojure wrappers for the arangodb java client and velocypack libs

Early statges an will definitely change

```clojure
(ns my.project.core
  (:require [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.databases :as databases]
            [clj-arangodb.arangodb.collections :as collections]
            [clj-arangodb.velocypack.core :as v]
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
    
(collections/insert-documents my-collection 
                              (map v/pack [{:name "Homer" :age 38}
                                           {:name "Marge" :age 36}
                                           {:name "Bart" :age 10}
                                           {:name "Lisa" :age 8}
                                           {:name "Maggie" :age 2}]))
  
(let [query-fn (aql/stmt {"one" 1 "two" 2 "@coll" coll-name}
                         "FOR i IN @@coll RETURN (i.age + @one) * @two")
      xs (.asListRemaining (query-fn db VPackSlice))]
  (map #(.getAsInt %) xs))
  
> (78 74 22 18 6)
```
