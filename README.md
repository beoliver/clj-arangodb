# clj-arangodb
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
  (let [conn (arango/connect {:user "dev" :password "123"})
        db-name "userDB"]
    (try (arango/drop-db conn db-name)
         (catch Exception e nil))
    (let [db (do (arango/create-db conn db-name)
                 (arango/get-db conn db-name))
          ;; we create a seq of maps with name and age keys
          the-simpons (map (fn [x y] {:name x :age y})
                           ["Homer" "Marge" "Bart" "Lisa" "Maggie"] [38 36 10 8 2])
          ;; we need to create a collection to keep the characers.
          ;; the map passed is optional
          ;; (create-collection will make a document collection by default)
          coll (do (d/create-collection db "Simpsons" {:type :document})
                   (d/get-collection db "Simpsons"))
          ;; we insert the docs and merge the return keys - giving us ids
          ;; we will use the first names as keys into the map
          simpsons-map (into {} (map (fn [m] [(:name m) m])
                                     (map merge
                                          the-simpons
                                          (c/insert-docs coll
                                                         (map v/pack the-simpons)))))
          ;; lets get the ids for all the characters
          bart (get-in simpsons-map ["Bart" :_id])
          lisa (get-in simpsons-map ["Lisa" :_id])
          maggie (get-in simpsons-map ["Maggie" :_id])
          homer (get-in simpsons-map ["Homer" :_id])
          marge (get-in simpsons-map ["Marge" :_id])
          ;; next we want to think about the relations that we want to capture
          ;; create two edge relations, each r is a is a new or existing edge collection
          ;; as an exaple we will create an edge collection called "Sibling" now.
          sibling-coll (do (d/create-collection db "Sibling" {:type :edge})
                           (d/get-collection db "Sibling"))
          r1 {:name "Sibling" :from ["Simpsons"] :to ["Simpsons"]}
          r2 {:name "Parent" :from ["Simpsons"] :to ["Simpsons"]}

          simpson-graph (do
                          (d/create-graph db "SimpsonGraph" [r1 r2])
                          (d/get-graph db "SimpsonGraph"))]

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
