(ns clj-arangodb.arangodb.databases
  (:require [clojure.set :as set]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.graph :as graph]
            [clj-arangodb.arangodb.conversions :as conv]
            [clojure.reflect :as r])
  (:import
   com.arangodb.velocypack.VPackSlice
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoCursor
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoGraph
   com.arangodb.ArangoCollection
   com.arangodb.model.DocumentReadOptions
   com.arangodb.entity.CollectionEntity
   com.arangodb.entity.GraphEntity
   com.arangodb.ArangoDBException
   com.arangodb.velocypack.VPackSlice
   com.arangodb.model.CollectionCreateOptions
   com.arangodb.model.AqlQueryOptions)
  (:refer-clojure :exclude [drop]))

(defn drop [^ArangoDatabase db] (.drop db))

(defn get-map
  ([^ArangoDatabase db id]
   (vpack/unpack (.getDocument db id VPackSlice) keyword))
  ([^ArangoDatabase db id key-fn]
   (vpack/unpack (.getDocument db id VPackSlice) key-fn)))

(defn get-document-as-map
  ([^ArangoDatabase db id]
   (vpack/unpack (.getDocument db id VPackSlice) keyword))
  ([^ArangoCollection db id key-fn]
   (vpack/unpack (.getDocument db id VPackSlice) key-fn)))

(defn get-document
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  "
  ([^ArangoDatabase db ^String id ^Class as]
   (.getDocument db id as))
  ([^ArangoDatabase db ^String id ^Class as ^DocumentReadOptions options]
   (.getDocument db id as options)))

(defn ^CollectionEntity create-collection
  "create a new collection entity"
  ([^ArangoDatabase db ^String coll-name]
   (.createCollection db coll-name))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (.createCollection db coll-name options)))

(defn ^ArangoCollection collection
  ([^ArangoDatabase db ^String coll-name]
   (.collection db coll-name)))

(def get-collection collection)

(defn ^ArangoCollection create-and-get-collection
  ([^ArangoDatabase db ^String coll-name]
   (do (.createCollection db coll-name)
       (.collection db coll-name)))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (do (.createCollection db coll-name options)
       (.collection db coll-name))))

(defn get-collections
  "when called with with a `post-fn` will map over the collection of
  `CollectionEntity`. By default these objects are converted into
  clojure maps. If you want the actual Objects, just pass `identity` as
  the post-fn"
  ([^ArangoDatabase db] (get-collections db conv/->map))
  ([^ArangoDatabase db post-fn] (vec (map post-fn (.getCollections db)))))

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
  "when called with with a `post-fn` will map over the collection of
  `GraphEntity`. By default these objects are converted into
  clojure maps. If you want the actual Objects, just pass `identity` as
  the post-fn"
  ([^ArangoDatabase db] (get-graphs db conv/->map))
  ([^ArangoDatabase db post-fn] (vec (map post-fn (.getGraphs db)))))

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
  (.createGraph db graph-name
                (map #(if (map? %) (graph/edge-definition %) %)
                     edge-definitions)))

(defn ^ArangoGraph graph
  ([^ArangoDatabase db ^String graph-name]
   (.graph db graph-name)))

(defn ^ArangoCursor query
  ;; can pass java.util.Map / java.util.List as well
  ([^ArangoDatabase db ^String query-str]
   (query db query-str nil nil VPackSlice))
  ([^ArangoDatabase db ^String query-str bindvars ^AqlQueryOptions options ^Class as]
   (.query db query-str bindvars options as)))
