(ns user
  (:require [clj-arangodb.arangodb.core :as arango]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.graph :as g]
            [clj-arangodb.velocypack.core :as v]))


(defn create-the-simpsons [db]
  (when-not (d/graph-exists? db "theSimpsonsGraph")
    (d/create-graph db
                    "theSimpsonsGrpah"
                    ;; define the edges (this creates new edge and vertex collections)
                    [{:name "siblings" :from ["characters"] :to ["characters"]}
                     {:name "parents" :from ["characters"] :to ["characters"]}]))
  (let [graph (d/graph db "theSimpsonsGrpah")]
    {:characters (g/vertex-collection graph "characters")
     :siblings (g/edge-collection graph "siblings")
     :parents (g/edge-collection graph "parents")}))

(defn example []
  (let [conn (arango/connect {:user "test"})
        db (do (when-not (arango/database? conn "testDB")
                 (arango/create-database conn "testDB"))
               (arango/db conn "testDB"))
        {:keys [characters siblings parants] :as collections} (create-the-simpsons db)]
    ;; we now need to create some data to put into the database
    (let [simpsons (into {} (map (fn [character]
                                   (let [result (c/insert-vertex characters character)]
                                     (clojure.pprint/pprint result)
                                     [(:name character) (merge character (select-keys result [:id :key]))]))
                                 [{:name "Homer"  :age 38}
                                  {:name "Marge"  :age 36}
                                  {:name "Bart"   :age 10}
                                  {:name "Lisa"   :age 8}
                                  {:name "Maggie" :age 2}]))
          siblings ["Bart" "Lisa" "Maggie"]]
      ;; we now have a map keyed by the names of the characters
      simpsons

      )

    )
  )


;; (defn example []
;;   (let [conn (arango/connect {:user "dev" :password "123"})]
;;     (arango/create-database conn "userDB")
;;     (let [db (arango/db conn "userDB")
;;     	  graph (do (d/create-graph "theSimpsonsGrpah" [{:name "Siblings" :from ["Characters"] :to ["Characters"]}
;; 	  		           		       	{:name "Parents" :from ["Characters"] :to ["Characters"]}])
;; 	 	    (d/graph "theSimpsonsGrpah"))
;;           characters [{:name "Homer"  :age 38}
;; 	  	      {:name "Marge"  :age 36}
;; 		      {:name "Bart"   :age 10}
;; 		      {:name "Lisa"   :age 8}
;; 		      {:name "Maggie" :age 2}]
;; 	  characters (g/vertex-collection graph "Characters")
;; 	  siblings   (g/edge-collection graph "Sibling")
;; 	  parents    (g/edge-collection graph "Parent")

;;           ;; we insert the docs and merge the return keys - giving us ids
;;           ;; we will use the first names as keys into the map
;;           simpsons-map (into {} (map (fn [m] [(:name m) m])
;;                                      (map merge
;;                                           the-simpsons
;;                                           (c/insert-docs coll
;;                                                          (map v/pack the-simpsons)))))
;;           ;; lets get the ids for all the characters
;;           bart (get-in simpsons-map ["Bart" :_id])
;;           lisa (get-in simpsons-map ["Lisa" :_id])
;;           maggie (get-in simpsons-map ["Maggie" :_id])
;;           homer (get-in simpsons-map ["Homer" :_id])
;;           marge (get-in simpsons-map ["Marge" :_id])
;;           ;; next we want to think about the relations that we want to capture
;;           ;; create two edge relations, each r is a is a new or existing edge collection
;;           ;; as an exaple we will create an edge collection called "Sibling" now.
;;           sibling-coll (d/create-and-get-collection db "Sibling" {:type :edge})]

;;       (d/create-graph db "SimpsonGraph" [{:name "Sibling" :from ["Simpsons"] :to ["Simpsons"]}
;;                                          {:name "Parent" :from ["Simpsons"] :to ["Simpsons"]}])

;;       (c/insert-docs sibling-coll (map v/pack [{:_from bart :_to lisa}
;;                                                {:_from lisa :_to bart}
;;                                                {:_from bart :_to maggie}
;;                                                {:_from maggie :_to bart}
;;                                                {:_from lisa :_to maggie}
;;                                                {:_from maggie :_to lisa}]))

;;       (-> (d/get-collection db "Parent")
;;           (c/insert-docs (flatten (for [parent [homer marge]]
;;                                     (for [child [bart lisa maggie]]
;;                                       (v/pack {:_from parent :_to child})))))))))
