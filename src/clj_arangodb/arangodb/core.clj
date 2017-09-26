(ns clj-arangodb.arangodb.core
  (:require [clj-arangodb.arangodb.builder :refer [build-new-arango-db]]))

(defn new-arrangodb
  [{:keys [host port user password] :as options}]
  (build-new-arango-db options))

(defn version
  "can be called on a `conn` or `database`, retuns a map with the keys
  `:liscense`, `:server`, `:version`"
  [conn]
  (-> (.getVersion conn) bean (dissoc :class) (update :license str)))

(defn users [conn]
  (map #(-> % bean (dissoc :class)) (.getUsers conn)))

(defn create-db
  "returns `true` on success"
  [conn db-name]
  (.createDatabase conn db-name))

(defn drop-db
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
  "returns a new map derived from `CollectionEntity`"
  ([db-obj coll-name]
   (-> db-obj (.createCollection coll-name nil)
       bean (dissoc :class) (update :status str) (update :type str)))
  ([conn db-name coll-name]
   (-> conn (.db db-name) (create-collection coll-name))))

(defn insert-document
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with the `:id` `:key` `:new` and `:rev`"
  ([coll-obj doc]
   (-> (.insertDocument coll-obj doc) bean (dissoc :class)))
  ([db-obj coll-name doc]
   (-> db-obj (.collection coll-name) (insert-document doc)))
  ([conn db-name coll-name doc]
   (-> conn (.db db-name) (insert-document coll-name doc))))

(defn insert-documents
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with the `:id` `:key` `:new` and `:rev`"
  ([coll-obj docs]
   (.insertDocuments coll-obj (java.util.ArrayList. docs)))
  ([db-obj coll-name docs]
   (-> db-obj (.collection coll-name) (insert-documents docs)))
  ([conn db-name coll-name docs]
   (-> conn (.db db-name) (insert-documents coll-name docs))))


;; (map :name (:members (r/reflect conn)))
