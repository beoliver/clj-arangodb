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

;; (map :name (:members (r/reflect conn)))
