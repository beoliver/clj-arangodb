(ns clj-arangodb.arangodb.graph
  (:require
   [clj-arangodb.velocypack.core :as v]
   [clj-arangodb.arangodb.options :as options]
   [clojure.set :as set]
   [clojure.reflect :as r])
  (:import com.arangodb.entity.EdgeDefinition
           com.arangodb.ArangoGraph
           com.arangodb.ArangoEdgeCollection
           com.arangodb.ArangoVertexCollection
           com.arangodb.model.VertexCreateOptions
           com.arangodb.model.EdgeCreateOptions
           com.arangodb.velocypack.VPackSlice
           com.arangodb.model.DocumentCreateOptions
           com.arangodb.model.DocumentReadOptions
           com.arangodb.model.DocumentReplaceOptions
           com.arangodb.model.DocumentUpdateOptions))

(defn make-multigraph [edge-collection-name edges]
  ;; an edge is of the from ["source" ["target_1" ... "target_n"]]
  (reduce (fn [edge-def [source targets]]
            (reduce (fn [edge-def target]
                      (.from edge-def source target)) edge-def targets))
          (new EdgeDefinition) edges))

;; (map :name (:members (r/reflect conn)))

(defn get-info [^ArangoGraph x] (bean (.getInfo x)))

(defn get-members [x] (map :name (:members (r/reflect x))))

(defn get-vertex-collections [^ArangoGraph graph]
  (.getVertexCollections graph))

(defn get-edge-definitions [^ArangoGraph graph]
  (.getEdgeDefinitions graph))

(defn edge-collection "get the actual collection" [^ArangoGraph graph coll-name]
  (.edgeCollection graph coll-name))

(defn vertex-collection "get the actual collection" [^ArangoGraph graph coll-name]
  (.vertexCollection graph coll-name))

(defn add-vertex-collection [^ArangoGraph graph ^String collection-name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (.addVertexCollection graph collection-name))

(defn edge-definition
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))

(defn get-edge
  ([^ArangoEdgeCollection coll ^String key ^Class as] (.getEdge coll key as))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (.getEdge coll key as (options/build DocumentReadOptions options))))

(defn insert-edge
  ([^ArangoEdgeCollection coll {:keys [_from _to] :as doc}]
   (.insertEdge coll doc))
  ([^ArangoEdgeCollection coll doc ^EdgeCreateOptions options]
   (.insertEdge coll doc (options/build EdgeCreateOptions options))))

(defn get-vertex
  [^ArangoVertexCollection coll key ^Class as]
  (.getVertex coll key as))

(defn insert-vertex
  ([^ArangoVertexCollection coll doc]
   (.insertVertex coll doc))
  ([^ArangoEdgeCollection coll doc ^VertexCreateOptions options]
   (.insertVertex coll doc (options/build VertexCreateOptions options))))
