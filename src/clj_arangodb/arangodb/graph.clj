(ns clj-arangodb.arangodb.graph
  (:require
   [clj-arangodb.velocypack.core :as vpack]
   [clj-arangodb.arangodb.adapter :refer [from-entity
                                          from-collection
                                          serialize-doc
                                          deserialize-doc]]
   [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            GraphEntity
            VertexEntity
            VertexUpdateEntity
            EdgeUpdateEntity
            EdgeEntity
            EdgeDefinition]
           [com.arangodb
            ArangoGraph
            ArangoVertexCollection
            ArangoEdgeCollection]
           [com.arangodb.model
            VertexCreateOptions
            VertexDeleteOptions
            VertexReplaceOptions
            VertexUpdateOptions
            EdgeCreateOptions
            EdgeDeleteOptions
            EdgeReplaceOptions
            EdgeUpdateOptions
            DocumentCreateOptions
            DocumentReadOptions
            DocumentReplaceOptions
            DocumentUpdateOptions])
  (:refer-clojure :exclude [drop]))

(defn ^Boolean exists? [^ArangoGraph x]
  (.exists x))

(defn ^GraphEntity get-info [^ArangoGraph x]
  (from-entity (.getInfo x)))

(defn drop [^ArangoGraph x]
  (.drop x))

(defn make-multigraph [edge-collection-name edges]
  ;; an edge is of the from ["source" ["target_1" ... "target_n"]]
  (reduce (fn [edge-def [source targets]]
            (reduce (fn [edge-def target]
                      (.from edge-def source target)) edge-def targets))
          (new EdgeDefinition) edges))

;; (map :name (:members (r/reflect conn)))

(defn get-vertex-collections [^ArangoGraph graph]
  ;; collection of strings
  (.getVertexCollections graph))

(defn ^ArangoVertexCollection vertex-collection
  "get the actual collection"
  [^ArangoGraph graph ^String name]
  (from-collection (.vertexCollection graph name)))

(defn ^GraphEntity add-vertex-collection [^ArangoGraph graph ^String name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (from-entity (.addVertexCollection graph name)))

(defn get-edge-definitions [^ArangoGraph graph]
  ;; collection of strings
  (.getEdgeDefinitions graph))

(defn ^ArangoEdgeCollection edge-collection
  "get the actual collection"
  [^ArangoGraph graph ^String name]
  (from-collection (.edgeCollection graph name)))

(defn ^EdgeDefinition edge-definition
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))

(defn ^GraphEntity add-edge-definition
  [^ArangoGraph graph ^EdgeDefinition definition]
  (from-entity (.addEdgeDefinition graph definition)))

(defn ^GraphEntity replace-edge-definition [^ArangoGraph graph ^EdgeDefinition definition]
  (from-entity (.replaceEdgeDefinition graph definition)))

(defn ^GraphEntity remove-edge-definition [^ArangoGraph graph ^String name]
  (from-entity (.removeEdgeDefinition graph name)))

(defn ^VertexEntity insert-vertex
  ([^ArangoVertexCollection coll doc]
   (from-entity (.insertVertex coll (serialize-doc doc))))
  ([^ArangoEdgeCollection coll doc ^VertexCreateOptions options]
   (from-entity (.insertVertex coll (serialize-doc doc)
                               (options/build VertexCreateOptions options)))))

(defn get-vertex
  ([^ArangoVertexCollection coll key]
   (get-vertex coll key VPackSlice))
  ([^ArangoVertexCollection coll ^String key ^Class as]
   (deserialize-doc (.getVertex coll key as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (deserialize-doc (.getVertex coll key as (options/build DocumentReadOptions options)))))

(defn ^VertexUpdateEntity replace-vertex
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (from-entity (.replaceVertex coll key (serialize-doc doc))))
  ([^ArangoVertexCollection coll ^String key ^Object doc ^VertexUpdateOptions options]
   (from-entity (.replaceVertex coll key (serialize-doc doc)
                                (options/build VertexUpdateOptions options)))))

(defn ^VertexUpdateEntity update-vertex
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (from-entity (.updateVertex coll key (serialize-doc doc))))
  ([^ArangoVertexCollection coll ^String key ^Object doc ^VertexUpdateOptions options]
   (from-entity (.updateVertex coll key (serialize-doc doc)
                               (options/build VertexUpdateOptions options)))))

(defn delete-vertex
  ;; void
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (.deleteVertex coll key (serialize-doc doc)))
  ([^ArangoVertexCollection coll ^String key ^Object doc ^VertexDeleteOptions options]
   (.deleteVertex coll key (serialize-doc doc)
                  (options/build VertexDeleteOptions options))))

(defn ^EdgeEntity insert-edge
  ([^ArangoEdgeCollection coll doc]
   (from-entity (.insertEdge coll (serialize-doc doc))))
  ([^ArangoEdgeCollection coll doc ^EdgeCreateOptions options]
   (from-entity (.insertEdge coll (serialize-doc doc)
                             (options/build EdgeCreateOptions options)))))

(defn get-edge
  ([^ArangoEdgeCollection coll key]
   (get-edge coll key VPackSlice))
  ([^ArangoEdgeCollection coll ^String key ^Class as]
   (deserialize-doc (.getEdge coll key as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (deserialize-doc (.getEdge coll key as (options/build DocumentReadOptions options)))))


(defn ^EdgeUpdateEntity replace-edge
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (from-entity (.replaceEdge coll key (serialize-doc doc))))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeUpdateOptions options]
   (from-entity (.replaceEdge coll key (serialize-doc doc)
                                (options/build EdgeUpdateOptions options)))))

(defn ^EdgeUpdateEntity update-edge
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (from-entity (.updateEdge coll key (serialize-doc doc))))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeUpdateOptions options]
   (from-entity (.updateEdge coll key (serialize-doc doc)
                               (options/build EdgeUpdateOptions options)))))

(defn delete-edge
  ;; void
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (.deleteEdge coll key (serialize-doc doc)))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeDeleteOptions options]
   (.deleteEdge coll key (serialize-doc doc)
                (options/build EdgeDeleteOptions options))))
