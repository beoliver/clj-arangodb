# clj-arangodb

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server. This library provides clojure developers a clean interface. Much like monger, the java implementation is still visible.

If we look at how the java code is used, in this example a new connecton is being made to a server.
```java
ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).host("192.168.182.50", 8888).build();
```
In the clojure version we are doing exactly the same thing under the hood.
```clojure
(def arango-db (connect {:useProtocol :vst :host "192.168.182.50" :port 8888}))
```
Where possible the keys to maps are identical to the methods in the java-driver. By default the java driver connectes to arangodb using something called a velocystream. this has some implications - firstly it is in theory more efficient, secondly we need to be aware of something called velocypacks - but we will come to that later.

So lets begin with a simple example.
```clojure
(require '[clj-arangodb.arangodb.core :as arango])
;; the .core ns provides functions for creating connections and working with databases
(def conn (arango/connect {:user "dev" :password "123"}))
;; we create a connection - this is for my local instace. If no credentials are used then
;; it falls back to the defaults for the java-driver ("root")
;; so I guess the first thing that we want to do is create a database.
(arango/create-db conn "userDB")
(def db (arango/get-db conn "userDB"))
;; as we quite like to get things back when we create them, there is a function
;; that performs these two operations behind the scenes.
(def some-other-db (arango/create-and-get-db conn "someOtherDB"))
;; db is a database "handle".
(def coll (d/create-and-get-collection db "Simpsons" {:type :document}))
```

Clojure wrappers for the arangodb java client and velocypack libs

Early statges an will definitely change

```clojure
(ns user
  (:require [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.graph :as g]
            [clj-arangodb.velocypack.core :as v]))

(defn example []
  (let [conn (arango/connect {:user "dev" :password "123"})]
    (arango/drop-db-if-exists conn "userDB")
    (let [db (arango/create-and-get-db conn "userDB")
          ;; we create a seq of maps with name and age keys
          the-simpsons (map (fn [x y] {:name x :age y})
                           ["Homer" "Marge" "Bart" "Lisa" "Maggie"] [38 36 10 8 2])
          ;; we need to create a collection to keep the characers.
          ;; the map passed is optional
          ;; (create-collection will make a document collection by default)
          coll (d/create-and-get-collection db "Simpsons" {:type :document})
          ;; we insert the docs and merge the return keys - giving us ids
          ;; we will use the first names as keys into the map
          simpsons-map (into {} (map (fn [m] [(:name m) m])
                                     (map merge
                                          the-simpsons
                                          (c/insert-docs coll
                                                         (map v/pack the-simpsons)))))
          ;; lets get the ids for all the characters
          bart (get-in simpsons-map ["Bart" :_id])
          lisa (get-in simpsons-map ["Lisa" :_id])
          maggie (get-in simpsons-map ["Maggie" :_id])
          homer (get-in simpsons-map ["Homer" :_id])
          marge (get-in simpsons-map ["Marge" :_id])
          ;; next we want to think about the relations that we want to capture
          ;; create two edge relations, each r is a is a new or existing edge collection
          ;; as an exaple we will create an edge collection called "Sibling" now.
          sibling-coll (d/create-and-get-collection db "Sibling" {:type :edge})]

      (d/create-graph db "SimpsonGraph" [{:name "Sibling" :from ["Simpsons"] :to ["Simpsons"]}
                                         {:name "Parent" :from ["Simpsons"] :to ["Simpsons"]}])

      (c/insert-docs sibling-coll (map v/pack [{:_from bart :_to lisa}
                                               {:_from lisa :_to bart}
                                               {:_from bart :_to maggie}
                                               {:_from maggie :_to bart}
                                               {:_from lisa :_to maggie}
                                               {:_from maggie :_to lisa}]))

      (-> (d/get-collection db "Parent")
          (c/insert-docs (flatten (for [parent [homer marge]]
                                    (for [child [bart lisa maggie]]
                                      (v/pack {:_from parent :_to child})))))))))
```
