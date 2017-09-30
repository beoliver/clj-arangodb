(ns clj-arangodb.arangodb.databases
  (:require [clojure.set :as set]
            [clj-arangodb.arangodb.graph :as graph]
            [pjson.core :as json]
            [clojure.walk :as walk]
            [clj-arangodb.arangodb.utils :as utils]
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
   com.arangodb.velocypack.VPackSlice))

(defn ^CollectionEntity create-coll
  ([^ArangoDatabase db ^String coll-name]
   (-> db (.createCollection coll-name nil))))

(defn ^GraphEntity create-graph
  "Create a new graph `graph-name`. edge-definitions must be a not empty
  sequence of maps `{:name 'relationName' :sources ['collA'...] :targets [collB...]}`
  if the names in sources and targets do not exist on the database, then new collections
  will be created."
  [^ArangoDatabase db graph-name edge-definitions]
  (.createGraph db graph-name
                (map #(if (map? %) (graph/define-edge %) %)
                     edge-definitions)))


(defn ^ArangoCollection get-coll
  "Always returns a new `ArrangoCollection` even if no such collection exists.
  The returned object can be used if a collection is created at a later time"
  ([^ArangoDatabase db ^String coll-name]
   (.collection db coll-name)))

(defn ^ArangoGraph get-graph
  "Always returns a new `ArrangoGraph` even if no such collection exists.
  The returned object can be used if a collection is created at a later time"
  ([^ArangoDatabase db ^String graph-name]
   (.graph db graph-name)))


(defn truncate-coll
  ""
  [db coll-name]
  (-> db (.collection coll-name) .truncate))

(defn drop-coll
  ""
  [db coll-name]
  (-> db (.collection coll-name) .drop))

(defn drop-graph [db graph-name]
  (-> (.graph db graph-name) .drop))

(defn ^ArangoCollection get-coll
  "returns a new `ArangoCollection`."
  ([^ArangoDatabase db ^String coll-name]
   (-> db (.collection coll-name)))
  ([^ArangoDB conn ^String db-name ^String coll-name]
   (-> conn (.db db-name) (.collection coll-name))))

(defn get-colls
  "returns a `lazySeq` of maps with keys
  `:id`, `:isSystem`, `:isVolatile`, `:name`, `:status`, `:type`, `:waitForSync`"
  [conn db-name]
  (map #(-> % bean
            (dissoc :class)
            (update :status str)
            (update :type str))
       (-> conn (.db db-name) .getCollections)))

(defn get-vpack-doc-by-id
  ([db id] (.getDocument db id VPackSlice))
  ([conn db-name id] (-> conn (.db db-name) (.getDocument id VPackSlice))))

(defn get-json-doc-by-id*
  [{:keys [conn db]} id]
  (-> conn (.db db) (.getDocument id String)))

(defn get-json-doc-by-id
  ([db id] (.getDocument db id String))
  ([conn db-name id]
   (-> conn (.db db-name) (.getDocument id String))))



(defn get-doc-by-id
  ([db id] (-> (get-json-doc-by-id db id)
               json/read-str
               walk/keywordize-keys))
  ([conn db-name id] (-> (get-json-doc-by-id conn db-name id)
                         json/read-str
                         walk/keywordize-keys)))

(def get-edge-by-id get-doc-by-id)



(defn get-bean-doc-by-id
  ([db id class] (.getDocument db id class))
  ([conn db-name id class] (-> conn (.db db-name) (.getDocument id class))))
