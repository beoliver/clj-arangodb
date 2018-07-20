(ns clj-arangodb.arangodb.databases
  (:require [clojure.set :as set]
            [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.graph :as g]
            [clojure.reflect :as r])
  (:import
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoGraph
   com.arangodb.ArangoCollection
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

(defn get-document
  [^ArangoDatabase db id ^Class as]
  (.getDocument db id as))

(defn ^CollectionEntity create-collection
  "create a new collection entity"
  ([^ArangoDatabase db ^String coll-name]
   (.createCollection db coll-name))
  ([^ArangoDatabase db ^String coll-name ^CollectionCreateOptions options]
   (.createCollection db coll-name options)))

(defn ^ArangoCollection collection
  ([^ArangoDatabase db ^String coll-name]
   (.collection db coll-name)))

(defn get-collections [^ArangoDatabase db] (.getCollections db))

(defn get-graphs [^ArangoDatabase db] (.getGraphs db))

(defn collection-exists? [^ArangoDatabase db collection-name]
  (some #(= collection-name (.getName %)) (get-collections db)))

(defn graph-exists? [^ArangoDatabase db graph-name]
  (some #(= graph-name (.getName %)) (get-graphs db)))

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

(defn query
  ;; can pass java.util.Map / java.util.List as well
  ([^ArangoDatabase db ^String query-str]
   (query db query-str nil nil VPackSlice))
  ([^ArangoDatabase db ^String query-str bindvars ^AqlQueryOptions options ^Class as]
   (.query db query-str bindvars options as)))
