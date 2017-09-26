# clj-arangodb
Clojure wrappers for the arangodb java client and velocypack libs

Early statges an will definitely change

```clojure
(ns my.project.core
  (:require [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.velocypack.core :as v]
            [clj-arangodb.arangodb.aql :as aql]))

(def conn (conn/new-arrangodb {}))
(arango/create-database conn "myDatabase")
(arango/create-collection conn "myDatabase" "myCollection")

(arango/insert-documents conn
                         "myDatabase"
                         "myCollection"
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
