(ns clj-arangodb.arangodb.extras
  (:require [clojure.set :as set]
            [pjson.core :as json]
            [clojure.walk :as walk]
            [clj-arangodb.arangodb.core :as a]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.graph :as g]
            [clojure.reflect :as r])
  (:import
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.ArangoDBException
   com.arangodb.velocypack.VPackSlice))

(defn drop-db-silently
  "returns `true` if database with `db-name` was dropped else `nil`"
  [^ArangoDB conn ^String db-name]
  (try (a/drop-db conn db-name)
       (catch ArangoDBException e nil)))

(defn drop-all-dbs-silently
  "attampts to drop all databases apart from '_system'. Does not report errors"
  [^ArangoDB conn]
  (doseq [db-name (a/get-dbs conn)]
    (when-not (= db-name "_system")
      (drop-db-silently conn db-name))))

(defn ^ArangoDatabase get-db-if-exists
  "returns a new `ArrangoDatabase` if the connection expects it to exist else `nil`"
  [^ArangoDB conn ^String db-name]
  (when (-> (a/get-dbs conn)
            set
            (contains? db-name))
    (a/get-db conn db-name)))
