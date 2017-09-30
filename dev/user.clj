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
          the-simpons (map (fn [x y] {:name x :age y})
                           ["Homer" "Marge" "Bart" "Lisa" "Maggie"] [38 36 10 8 2])
          ;; we need to create a collection to keep the characers.
          ;; the map passed is optional
          ;; (create-collection will make a document collection by default)
          coll (d/create-and-get-collection db "Simpsons" {:type :document})
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


;; (def conn (arango/connect {:user "dev" :password "123"}))

;; (def db-name "userDB")
;; (try (arango/drop-db conn db-name) (catch Exception e nil))

;; (arango/create-db conn db-name)
;; (def db (arango/get-db conn db-name))

;; (def coll-name "Simpsons")
;; (try (d/drop-coll db coll-name) (catch Exception e nil))
;; (d/create-coll db coll-name)
;; (def coll (d/get-coll db coll-name))

;; (def the-simpons [{:name "Homer" :age 38}
;;                   {:name "Marge" :age 36}
;;                   {:name "Bart" :age 10}
;;                   {:name "Lisa" :age 8}
;;                   {:name "Maggie" :age 2}])

;; (def simpson-insert-data
;;   )

;; (def simpson-map
;;   (into {} (map (fn [m] [(:name m) m]) simpson-insert-data)))

;; (def example-key (get-in simpson-map ["Homer" :_id]))

;; ;;; is there an option to create undirected edges? A->B and B->A
;; (def sibling-relation
;;   (graph/define-edge {:name "Sibling"
;;                       :from ["Simpsons"]
;;                       :to ["Simpsons"]}))

;; (def parent-relation
;;   (graph/define-edge {:name "Parent"
;;                       :from ["Simpsons"]
;;                       :to ["Simpsons"]}))

;; (def graph-entity
;;   (d/create-graph db "simpson-graph" [sibling-relation parent-relation]))

;; (def siblings
;;   (let [bart (get-in simpson-map ["Bart" :_id])
;;         lisa (get-in simpson-map ["Lisa" :_id])
;;         maggie (get-in simpson-map ["Maggie" :_id])]
;;     (map v/pack [{:_from bart :_to lisa}
;;                  {:_from lisa :_to bart}
;;                  {:_from bart :_to maggie}
;;                  {:_from maggie :_to bart}
;;                  {:_from lisa :_to maggie}
;;                  {:_from maggie :_to lisa}])))

;; (def simpson-graph (d/get-graph db "simpson-graph"))

;; (def sibling-coll (g/get-edge-coll simpson-graph "Sibling"))

;; (doseq [e siblings]
;;   (g/add-edge sibling-coll e))

;; (def the-parents
;;   (let [homer (get-in simpson-map ["Homer" :_id])
;;         marge (get-in simpson-map ["Marge" :_id])
;;         bart (get-in simpson-map ["Bart" :_id])
;;         lisa (get-in simpson-map ["Lisa" :_id])
;;         maggie (get-in simpson-map ["Maggie" :_id])]
;;     (map v/pack [{:_from homer :_to bart}
;;                  {:_from homer :_to lisa}
;;                  {:_from homer :_to maggie}
;;                  {:_from marge :_to bart}
;;                  {:_from marge :_to maggie}
;;                  {:_from marge :_to lisa}])))

;; (let [edge-coll (d/get-coll db "Parent")]
;;   (c/insert-docs edge-coll the-parents))




;; (def coll (do (a/create-coll db "Simpsons")
;;               (a/get-coll db "Simpsons")))

;; (def datamaps (a/insert-docs coll testdata))

;; ;; Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();

;; ;; EdgeDefinition edgeDefinition = new EdgeDefinition();

;; (def edge-definitions [(let [e (new EdgeDefinition)]
;;                          (do (.collection e "Wololo")
;;                              (.from e (into-array ["Simpsons"]))
;;                              (.to e (into-array ["A"]))))])


;; // define the edgeCollection to store the edges
;; edgeDefinition.collection("myEdgeCollection");
;; // define a set of collections where an edge is going out...
;; edgeDefinition.from("myCollection1", "myCollection2");

;; // repeat this for the collections where an edge is going into
;; edgeDefinition.to("myCollection1", "myCollection3");

;; edgeDefinitions.add(edgeDefinition);

;; // A graph can contain additional vertex collections, defined in the set of orphan collections
;; GraphCreateOptions options = new GraphCreateOptions();
;; options.orphanCollections("myCollection4", "myCollection5");

;; // now it's possible to create a graph
;; arangoDB.db("myDatabase").createGraph("myGraph", edgeDefinitions, options);
