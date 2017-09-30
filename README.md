# clj-arangodb
Clojure wrappers for the arangodb java client and velocypack libs

Early statges an will definitely change

```clojure
(ns user
  (:require [clojure.reflect :as r]
            [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.extras :as extras]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.graph :as g]
            [clj-arangodb.velocypack.core :as v]
            [clj-arangodb.arangodb.graph :as graph])
  (:import com.arangodb.velocypack.VPackSlice
           com.arangodb.entity.EdgeDefinition))


(defn example []
  (let [conn (arango/connect {:user "dev" :password "123"})
        db-name "userDB"]
    (try (arango/drop-db conn db-name)
         (catch Exception e nil))
    (arango/create-db conn db-name)
    (let [db (arango/get-db conn db-name)
          coll-name "Simpsons"
          coll (do (d/create-coll db coll-name)
                   (d/get-coll db coll-name))
          the-simpons [{:name "Homer" :age 38}
                       {:name "Marge" :age 36}
                       {:name "Bart" :age 10}
                       {:name "Lisa" :age 8}
                       {:name "Maggie" :age 2}]

          simpsons-map (into {} (map (fn [m] [(:name m) m])
                                     (map merge
                                          the-simpons
                                          (c/insert-docs coll
                                                         (map v/pack the-simpons)))))
          bart (get-in simpsons-map ["Bart" :_id])
          lisa (get-in simpsons-map ["Lisa" :_id])
          maggie (get-in simpsons-map ["Maggie" :_id])
          homer (get-in simpsons-map ["Homer" :_id])
          marge (get-in simpsons-map ["Marge" :_id])
          ;; create two edge relations, each r is a is a new or existing edge collection
          r1 {:name "Sibling" :from ["Simpsons"] :to ["Simpsons"]}
          r2 {:name "Parent" :from ["Simpsons"] :to ["Simpsons"]}]

      (d/create-graph db "SimpsonGraph" [r1 r2])
      (let [simpson-graph (d/get-graph db "SimpsonGraph")
            sibling-coll (g/get-edge-coll simpson-graph "Sibling")
            parent-coll (g/get-edge-coll simpson-graph "Parent")]

        (doseq [edge (map v/pack [{:_from bart :_to lisa}
                                  {:_from lisa :_to bart}
                                  {:_from bart :_to maggie}
                                  {:_from maggie :_to bart}
                                  {:_from lisa :_to maggie}
                                  {:_from maggie :_to lisa}])]
          (g/add-edge sibling-coll edge))

        ;; can just insert multiple edges using the d/get-coll
        ;; better performance
        (-> (d/get-coll db "Parent")
            (c/insert-docs (flatten (for [parent [homer marge]]
                                      (for [child [bart lisa maggie]]
                                        (v/pack {:_from parent :_to child}))))))))))

```
