(ns clj-arangodb.arangodb.databases
  (:require [clojure.set :as set]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.arangodb.utils :refer [maybe-vpack]]
            [clj-arangodb.arangodb.conversions :refer [->result]]
            [clj-arangodb.arangodb.options :as options]
            [clojure.reflect :as r])
  (:import
   com.arangodb.velocypack.VPackSlice
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoCursor
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoGraph
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.entity.GraphEntity
   com.arangodb.ArangoDBException
   com.arangodb.velocypack.VPackSlice
   [com.arangodb.model
    DocumentReadOptions
    CollectionCreateOptions
    AqlQueryOptions])
  (:refer-clojure :exclude [drop]))

(defn drop [^ArangoDatabase db]
  (->result (.drop db)))

(defn get-document
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  "
  ([^ArangoDatabase db ^String id]
   (get-document db id VPackSlice))
  ([^ArangoDatabase db ^String id ^Class as]
   (->result (.getDocument db id as)))
  ([^ArangoDatabase db ^String id ^Class as ^DocumentReadOptions options]
   (->result (.getDocument db id as options))))

(defn ^CollectionEntity create-collection
  "create a new collection entity"
  ([^ArangoDatabase db ^String coll-name]
   (->result (.createCollection db coll-name)))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (->result (.createCollection db coll-name (options/build CollectionCreateOptions options)))))

(defn ^ArangoCollection collection
  ([^ArangoDatabase db ^String coll-name]
   (->result (.collection db coll-name))))

(def get-collection collection)

(defn ^ArangoCollection create-and-get-collection
  ([^ArangoDatabase db ^String coll-name]
   (do (.createCollection db coll-name)
       (->result (.collection db coll-name))))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (do (.createCollection db coll-name (options/build CollectionCreateOptions options))
       (->result (.collection db coll-name)))))

(defn get-collections
  [^ArangoDatabase db]
  (->result (.getCollections db)))

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

(defn get-graphs
  [^ArangoDatabase db]
  (->result (.getGraphs db)))

(defn collection-exists? [^ArangoDatabase db collection-name]
  (some #(= collection-name (.getName %)) (get-collections db identity)))

(defn graph-exists? [^ArangoDatabase db graph-name]
  (some #(= graph-name (.getName %)) (get-graphs db identity)))

(defn ^GraphEntity create-graph
  "Create a new graph `graph-name`. edge-definitions must be a non empty
  sequence of maps `{:name 'relationName' :from ['collA'...] :to [collB...]}`
  if the names in sources and targets do not exist on the database, then new collections
  will be created."
  [^ArangoDatabase db graph-name edge-definitions]
  (->result (.createGraph db graph-name
                          (map #(if (map? %) (graph/edge-definition %) %)
                               edge-definitions))))

(defn ^ArangoGraph graph
  ([^ArangoDatabase db ^String graph-name]
   (->result (.graph db graph-name))))

(defn ^ArangoCursor query
  ;; can pass java.util.Map / java.util.List as well
  ([^ArangoDatabase db ^String query-str]
   (query db query-str nil nil VPackSlice))
  ([^ArangoDatabase db ^String query-str bindvars ^AqlQueryOptions options ^Class as]
   (->result (.query db query-str bindvars (options/build AqlQueryOptions options) as))))
