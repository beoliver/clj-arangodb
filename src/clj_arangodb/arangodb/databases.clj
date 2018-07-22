(ns clj-arangodb.arangodb.databases
  (:require [clojure.set :as set]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.options :as options]
            [clojure.reflect :as r])
  (:import
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoCursor
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoGraph
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.entity.GraphEntity
   com.arangodb.ArangoDBException
   [com.arangodb.entity
    DatabaseEntity]
   [com.arangodb.model
    DocumentReadOptions
    CollectionCreateOptions
    CollectionsReadOptions
    GraphCreateOptions
    AqlQueryOptions])
  (:refer-clojure :exclude [drop]))

(defn ^Boolean exists? [^ArangoDatabase db] (.exists db))

(defn ^Boolean drop [^ArangoDatabase db] (.drop db))

(defn ^DatabaseEntity get-info [^ArangoDatabase db] (ad/from-entity (.getInfo db)))

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
   (ad/deserialize-doc (.getDocument db id as options))))

(defn ^CollectionEntity create-collection
  "create a new collection entity"
  ([^ArangoDatabase db ^String coll-name]
   (ad/from-entity (.createCollection db coll-name)))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (ad/from-entity (.createCollection db coll-name
                                      (options/build CollectionCreateOptions options)))))

(defn ^ArangoCollection collection
  ([^ArangoDatabase db ^String coll-name]
   (.collection db coll-name)))

(def get-collection collection)

(defn ^ArangoCollection create-and-get-collection
  ([^ArangoDatabase db ^String coll-name]
   (do (.createCollection db coll-name)
       (.collection db coll-name)))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (do (.createCollection db coll-name (options/build CollectionCreateOptions options))
       (.collection db coll-name))))

(defn ^java.util.Collection get-collections
  ([^ArangoDatabase db]
   (map ad/from-entity (.getCollections db)))
  ([^ArangoDatabase db ^CollectionsReadOptions options]
   (map ad/from-entity
        (.getCollections db (options/build CollectionsReadOptions options)))))

(defn get-collection-names
  "returns a vector of `string`"
  [^ArangoDatabase db]
  (get-collections db #(.getName %)))

(defn collection-type
  [^ArangoDatabase db ^String collection-name]
  (reduce (fn [_ o]
            (if (= collection-name (.getName o))
              (reduced (str (.getType o)))
              nil)) nil (get-collections db identity)))

(defn ^java.util.Collection get-graphs
  [^ArangoDatabase db]
  (map ad/from-entity (.getGraphs db)))

(defn collection-exists? [^ArangoDatabase db collection-name]
  (some #(= collection-name (.getName %)) (get-collections db identity)))

(defn graph-exists? [^ArangoDatabase db graph-name]
  (some #(= graph-name (.getName %)) (get-graphs db identity)))

(defn ^GraphEntity create-graph
  "Create a new graph `graph-name`. edge-definitions must be a non empty
  sequence of maps `{:name 'relationName' :from ['collA'...] :to [collB...]}`
  if the names in sources and targets do not exist on the database, then new collections
  will be created."
  [^ArangoDatabase db ^String name edge-definitions ^GraphCreateOptions options]
  (ad/from-entity (.createGraph db name
                                (map #(if (map? %) (graph/edge-definition %) %)
                                     edge-definitions)
                                (options/build GraphCreateOptions options))))

(defn ^ArangoGraph graph
  ([^ArangoDatabase db ^String graph-name]
   (ad/from-graph (.graph db graph-name))))

(defn ^ArangoCursor query
  ;; can pass java.util.Map / java.util.List as well
  ([^ArangoDatabase db ^String query-str]
   (query db query-str nil nil ad/*default-doc-class*))
  ([^ArangoDatabase db ^String query-str bindvars ^AqlQueryOptions options ^Class as]
   (ad/from-cursor (.query db query-str bindvars (options/build AqlQueryOptions options) as))))
