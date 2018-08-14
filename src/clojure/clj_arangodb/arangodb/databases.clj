(ns clj-arangodb.arangodb.databases
  (:require [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.arangodb.aql :as aql]
            [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb
            ArangoDB
            ArangoCursor
            ArangoDatabase
            ArangoGraph
            ArangoCollection]
           [com.arangodb.entity
            GraphEntity
            CollectionEntity
            DatabaseEntity]
           [com.arangodb.model
            DocumentReadOptions
            CollectionCreateOptions
            CollectionsReadOptions
            GraphCreateOptions
            AqlQueryOptions])
  (:refer-clojure :exclude [drop]))

(defn exists? ^Boolean
  [^ArangoDatabase db] (.exists db))

(defn drop ^Boolean
  [^ArangoDatabase db] (.drop db))

(defn get-info ^DatabaseEntity
  [^ArangoDatabase db] (ad/from-entity (.getInfo db)))

(defn get-document
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  "
  ([^ArangoDatabase db ^String id]
   (get-document db id ad/*default-doc-class*))
  ([^ArangoDatabase db ^String id ^Class as]
   (ad/deserialize-doc (.getDocument db id as)))
  ([^ArangoDatabase db ^String id ^Class as ^DocumentReadOptions options]
   (ad/deserialize-doc (.getDocument db id as (options/build DocumentReadOptions options)))))

(defn ^CollectionEntity create-collection
  "create a new collection entity"
  ([^ArangoDatabase db ^String coll-name]
   (ad/from-entity (.createCollection db coll-name)))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (ad/from-entity (.createCollection db coll-name
                                      (options/build CollectionCreateOptions options)))))

(defn collection ^ArangoCollection
  ([^ArangoDatabase db ^String coll-name]
   (.collection db coll-name)))

(def get-collection collection)

(defn create-and-get-collection ^ArangoCollection
  ([^ArangoDatabase db ^String coll-name]
   (do (.createCollection db coll-name)
       (.collection db coll-name)))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (do (.createCollection db coll-name (options/build CollectionCreateOptions options))
       (.collection db coll-name))))

(defn get-collections
  ;; returns a collection of CollectionEntity
  ([^ArangoDatabase db]
   (vec (map ad/from-entity (.getCollections db))))
  ([^ArangoDatabase db ^CollectionsReadOptions options]
   (vec (map ad/from-entity
             (.getCollections db (options/build CollectionsReadOptions options))))))

(defn get-collection-names
  ([^ArangoDatabase db]
   (vec (map #(.getName ^CollectionEntity %) (.getCollections db)))))

(defn get-graphs
  ;; returns a collection of GraphEntity
  [^ArangoDatabase db]
  (vec (map ad/from-entity (.getGraphs db))))

(defn collection-exists? [^ArangoDatabase db collection-name]
  (some #(= collection-name (.getName ^CollectionEntity %)) (.getCollections db)))

(defn graph-exists? [^ArangoDatabase db graph-name]
  (some #(= graph-name (.getName ^GraphEntity %)) (.getGraphs db)))

(defn create-graph
  "Create a new graph `graph-name`. edge-definitions must be a non empty
  sequence of maps `{:name 'relationName' :from ['collA'...] :to [collB...]}`
  if the names in sources and targets do not exist on the database,
  then new collections will be created."
  ^GraphEntity
  [^ArangoDatabase db ^String name edge-definitions ^GraphCreateOptions options]
  (ad/from-entity (.createGraph db name
                                (map #(if (map? %) (graph/edge-definition %) %)
                                     edge-definitions)
                                (options/build GraphCreateOptions options))))

(defn graph ^ArangoGraph
  ([^ArangoDatabase db ^String graph-name]
   (.graph db graph-name)))

(def get-graph graph)

(defn query ^ArangoCursor
  ;; can pass java.util.Map / java.util.List as well
  ([^ArangoDatabase db aql-query]
   (query db aql-query nil nil ad/*default-doc-class*))
  ([^ArangoDatabase db aql-query ^Class as]
   (query db aql-query nil nil as))
  ([^ArangoDatabase db aql-query ^AqlQueryOptions options ^Class as]
   (query db aql-query nil options as))
  ([^ArangoDatabase db aql-query bindvars ^AqlQueryOptions options ^Class as]
   (.query db ^String (aql/serialize aql-query) bindvars (options/build AqlQueryOptions options) as)))
