(ns clj-arangodb.arangodb.graph
  (:require
   [clj-arangodb.velocypack.core :as vpack]
   [clj-arangodb.arangodb.utils :refer [maybe-vpack]]
   [clj-arangodb.arangodb.conversions :refer [->result]]
   [clj-arangodb.arangodb.options :as options])
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

(defn get-info [^ArangoGraph x]
  (->result (.getInfo x)))

(defn get-vertex-collections [^ArangoGraph graph]
  (->result (.getVertexCollections graph)))

(defn get-edge-definitions [^ArangoGraph graph]
  (->result (.getEdgeDefinitions graph)))

(defn edge-collection "get the actual collection" [^ArangoGraph graph coll-name]
  (->result (.edgeCollection graph coll-name)))

(defn vertex-collection "get the actual collection" [^ArangoGraph graph coll-name]
  (->result (.vertexCollection graph coll-name)))

(defn add-vertex-collection [^ArangoGraph graph ^String collection-name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (->result (.addVertexCollection graph collection-name)))

(defn edge-definition
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))

(defn get-edge
  ([^ArangoEdgeCollection coll key]
   (get-edge coll key VPackSlice))
  ([^ArangoEdgeCollection coll ^String key ^Class as]
   (->result (.getEdge coll key as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (->result (.getEdge coll key as (options/build DocumentReadOptions options)))))

(defn insert-edge
  ([^ArangoEdgeCollection coll {:keys [_from _to] :as doc}]
   (->result (.insertEdge coll (maybe-vpack doc))))
  ([^ArangoEdgeCollection coll doc ^EdgeCreateOptions options]
   (->result (.insertEdge coll (maybe-vpack doc)
                          (options/build EdgeCreateOptions options)))))

(defn get-vertex
  ([^ArangoVertexCollection coll key]
   (get-vertex coll key VPackSlice))
  ([^ArangoVertexCollection coll ^String key ^Class as]
   (->result (.getVertex coll ^String key ^Class as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (->result (.getVertex coll key as (options/build DocumentReadOptions options)))))

(defn insert-vertex
  ([^ArangoVertexCollection coll doc]
   (->result (.insertVertex coll (maybe-vpack doc))))
  ([^ArangoEdgeCollection coll doc ^VertexCreateOptions options]
   (->result (.insertVertex coll (maybe-vpack doc)
                            (options/build VertexCreateOptions options)))))
