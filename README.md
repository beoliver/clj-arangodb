# clj-arangodb
Clojure wrappers for the arangodb java client and velocypack libs

Early statges an will definitely change

```clojure
(ns my.project.core
  (:require [clj-arangodb.arangodb.core :as a]
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

;; a query is a function that takes a database and a return type
;; in this example we are going to return VPackSlices

(def my-query (aql/stmt {"one" 1 "two" 2 "@coll" coll-name}
                        "FOR i IN @@coll RETURN (i.age + @one) * @two"))

;; as we need a database, we could either pass one directly or use
;; an object. Ignore the .asListRemaining - it will be hidden.

(def my-database (arango/get-database conn "myDatabase"))

(map #(v/read-as % :int) (.asListRemaining (query-fn db VPackSlice)))

> (78 74 22 18 6)

;; the nice thing about VPackSlices is that we can "reach in" and grab
;; values without parsing the whole structure. It doesn't make a
;; difference in this case, but if we returned "maps" then we could
;; pick out certain keys.
```
