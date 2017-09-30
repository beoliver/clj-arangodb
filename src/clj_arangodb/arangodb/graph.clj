(ns clj-arangodb.arangodb.graph
  (:require
   [clj-arangodb.velocypack.core :as v]
   [pjson.core :as json]
   [clojure.set :as set]
   [clojure.reflect :as r])
  (:import com.arangodb.entity.EdgeDefinition
           com.arangodb.velocypack.VPackSlice))

(defn make-multigraph [edge-collection-name edges]
  ;; an edge is of the from ["source" ["target_1" ... "target_n"]]
  (reduce (fn [edge-def [source targets]]
            (reduce (fn [edge-def target]
                      (.from edge-def source target)) edge-def targets))
          (new EdgeDefinition) edges))

(defn get-graph [conn db-name graph-name]
  (-> conn
      (.db db-name)
      (.graph graph-name)))

;; (map :name (:members (r/reflect conn)))

(defn get-info [x] (bean (.getInfo x)))

(defn get-members [x] (map :name (:members (r/reflect x))))

(defn get-vertex-coll-names [g]
  (.getVertexCollections g))

(defn get-edge-def-names [g]
  (.getEdgeDefinitions g))


(defn add-vertex-coll [conn db-name graph-name coll-name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (-> conn (.db db-name) (.graph graph-name) (.addVertexCollection coll-name)))

(defn define-edge
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))

(defn insertion-data-as-map [obj]
  (-> obj bean (dissoc :class)
      (set/rename-keys
       {:id :_id :key :_key :new :_new :rev :_rev})))

(defn add-edge
  "vpack or json (or pojo). data MUST contain str keys '_from' and '_to'
  values must be keys not ids"
  ([edge-coll data]
   (insertion-data-as-map (.insertEdge edge-coll data)))
  ([graph edge-coll-name data]
   (add-edge (.edgeCollection graph edge-coll-name) data))
  ([db graph-name edge-coll data]
   (add-edge (.graph db graph-name) edge-coll data))
  ([conn db-name graph-name edge-coll data]
   (add-edge (.db conn db-name) graph-name edge-coll data)))

(defn add-edge*
  [{:keys [conn db coll]} data]
  (-> conn (.db db) (.collection coll) (.insertDocument data nil)
      bean
      (dissoc :class)
      (set/rename-keys {:id :_id :key :_key :new :_new :rev :_rev})))

(defn get-edge-by-key
  [conn db-name graph-name edge-collection-name key]
  (-> conn
      (.db db-name)
      (.graph graph-name)
      (.edgeCollection edge-collection-name)
      (.getEdge key String)))
