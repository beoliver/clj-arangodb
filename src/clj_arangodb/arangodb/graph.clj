(ns clj-arangodb.arangodb.graph
  (:require [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb.entity
            GraphEntity
            VertexEntity
            EdgeEntity
            EdgeDefinition
            VertexUpdateEntity
            EdgeUpdateEntity]
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

(defn ^Boolean exists? [^ArangoGraph x] (.exists x))

(defn ^GraphEntity get-info [^ArangoGraph x] (ad/from-entity (.getInfo x)))

(defn drop [^ArangoGraph x] (.drop x))

(defn get-vertex-collections [^ArangoGraph graph]
  ;; collection of strings
  (.getVertexCollections graph))

(defn ^ArangoVertexCollection vertex-collection
  "get the actual collection"
  [^ArangoGraph graph ^String name]
  (.vertexCollection graph name))

(defn ^GraphEntity add-vertex-collection [^ArangoGraph graph ^String name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (ad/from-entity (.addVertexCollection graph name)))

(defn get-edge-definitions [^ArangoGraph graph]
  ;; collection of strings
  (.getEdgeDefinitions graph))

(defn ^ArangoEdgeCollection edge-collection
  "get the actual collection"
  [^ArangoGraph graph ^String name]
  (.edgeCollection graph name))

(defn ^EdgeDefinition edge-definition
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))

(defn ^GraphEntity add-edge-definition
  [^ArangoGraph graph ^EdgeDefinition definition]
  (ad/from-entity (.addEdgeDefinition graph definition)))

(defn ^GraphEntity replace-edge-definition [^ArangoGraph graph ^EdgeDefinition definition]
  (ad/from-entity (.replaceEdgeDefinition graph definition)))

(defn ^GraphEntity remove-edge-definition [^ArangoGraph graph ^String name]
  (ad/from-entity (.removeEdgeDefinition graph name)))

(defn ^VertexEntity insert-vertex
  ([^ArangoVertexCollection coll doc]
   (ad/from-entity (.insertVertex coll (ad/serialize-doc doc))))
  ([^ArangoEdgeCollection coll doc ^VertexCreateOptions options]
   (ad/from-entity (.insertVertex coll (ad/serialize-doc doc)
                                  (options/build VertexCreateOptions options)))))

(defn get-vertex
  ([^ArangoVertexCollection coll key]
   (get-vertex coll key ad/*default-doc-class*))
  ([^ArangoVertexCollection coll ^String key ^Class as]
   (ad/deserialize-doc (.getVertex coll key as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (ad/deserialize-doc (.getVertex coll key as (options/build DocumentReadOptions options)))))

(defn ^VertexUpdateEntity replace-vertex
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (ad/from-entity (.replaceVertex coll key (ad/serialize-doc doc))))
  ([^ArangoVertexCollection coll ^String key ^Object doc ^VertexUpdateOptions options]
   (ad/from-entity (.replaceVertex coll key (ad/serialize-doc doc)
                                   (options/build VertexUpdateOptions options)))))

(defn ^VertexUpdateEntity update-vertex
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (ad/from-entity (.updateVertex coll key (ad/serialize-doc doc))))
  ([^ArangoVertexCollection coll ^String key ^Object doc ^VertexUpdateOptions options]
   (ad/from-entity (.updateVertex coll key (ad/serialize-doc doc)
                                  (options/build VertexUpdateOptions options)))))

(defn delete-vertex
  ;; void
  ([^ArangoVertexCollection coll ^String key ^Object doc]
   (.deleteVertex coll key (ad/serialize-doc doc)))
  ([^ArangoVertexCollection coll ^String key ^Object doc ^VertexDeleteOptions options]
   (.deleteVertex coll key (ad/serialize-doc doc)
                  (options/build VertexDeleteOptions options))))

(defn ^EdgeEntity insert-edge
  ([^ArangoEdgeCollection coll doc]
   (ad/from-entity (.insertEdge coll (ad/serialize-doc doc))))
  ([^ArangoEdgeCollection coll doc ^EdgeCreateOptions options]
   (ad/from-entity (.insertEdge coll (ad/serialize-doc doc)
                                (options/build EdgeCreateOptions options)))))

(defn get-edge
  ([^ArangoEdgeCollection coll key]
   (get-edge coll key ad/*default-doc-class*))
  ([^ArangoEdgeCollection coll ^String key ^Class as]
   (ad/deserialize-doc (.getEdge coll key as)))
  ([^ArangoEdgeCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (ad/deserialize-doc (.getEdge coll key as (options/build DocumentReadOptions options)))))


(defn ^EdgeUpdateEntity replace-edge
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (ad/from-entity (.replaceEdge coll key (ad/serialize-doc doc))))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeUpdateOptions options]
   (ad/from-entity (.replaceEdge coll key (ad/serialize-doc doc)
                                 (options/build EdgeUpdateOptions options)))))

(defn ^EdgeUpdateEntity update-edge
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (ad/from-entity (.updateEdge coll key (ad/serialize-doc doc))))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeUpdateOptions options]
   (ad/from-entity (.updateEdge coll key (ad/serialize-doc doc)
                                (options/build EdgeUpdateOptions options)))))

(defn delete-edge
  ;; void
  ([^ArangoEdgeCollection coll ^String key ^Object doc]
   (.deleteEdge coll key (ad/serialize-doc doc)))
  ([^ArangoEdgeCollection coll ^String key ^Object doc ^EdgeDeleteOptions options]
   (.deleteEdge coll key (ad/serialize-doc doc)
                (options/build EdgeDeleteOptions options))))
