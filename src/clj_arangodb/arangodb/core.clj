(ns clj-arangodb.arangodb.core
  (:require [clj-arangodb.arangodb.builder :refer [build-new-arango-db]]))

(defn new-arrangodb
  [{:keys [host port user password] :as options}]
  (build-new-arango-db options))

(defn create-database
  [conn db-name]
  (.createDatabase conn db-name))

(defn drop-database
  "returns `true` if database with `db-name` was dropped"
  [conn db-name]
  (-> conn (.db db-name) .drop))

(defn get-databases
  "returns a vector of database db-names"
  [conn] (into [] (.getDatabases conn)))

(defn get-database
  "returns a new `ArrangoDatabase` even if `db-name` does not exist."
  [conn db-name]
  (-> conn (.db db-name)))

(defn create-collection
  "returns a new `CollectionEntity`"
  ([db coll-name]
   (-> db (.createCollection coll-name nil)))
  ([conn db-name coll-name]
   (create-collection (-> conn (.db db-name)) coll-name)))

(defn truncate-collection
  ""
  ([db coll-name]
   (-> db (.collection coll-name) .truncate))
  ([conn db-name coll-name]
   (truncate-collection (-> conn (.db db-name)) coll-name)))

(defn drop-collection
  ""
  ([db coll-name]
   (-> db (.collection coll-name) .drop))
  ([conn db-name coll-name]
   (drop-collection (-> conn (.db db-name)) coll-name)))

(defn get-collection
  "returns a new `ArrangoCollection`."
  ([db coll-name]
   (-> db (.collection coll-name)))
  ([conn db-name coll-name]
   (get-collection (-> conn (.db db-name)) coll-name)))
