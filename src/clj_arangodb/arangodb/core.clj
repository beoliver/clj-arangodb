(ns clj-arangodb.arangodb.core
  (:require [clojure.set :as set]
            [clj-arangodb.arangodb.options :as options]
            [clj-arangodb.arangodb.conversions :as conv]
            [clj-arangodb.arangodb.utils :as utils])
  (:import [com.arangodb
            ArangoDB$Builder
            ArangoDB
            ArangoDatabase]
           com.arangodb.entity.BaseDocument))

(defn ^ArangoDB connect
  "
  Takes an optional map that may contain the following:
  keys have the same names as the java methods.
  :host a pair default is ['127.0.0.1' 8529]
  :user a String default is 'root'
  :password String by default no password is used
  :use-protocol vst | http-json | http-vpack (:vst by default)
  :ssl-context SSlContext not used
  :timeout Integer | Long
  :chunksize Integer | Long
  :max-connections Integer | Long
  "
  ([] (connect {}))
  ([options] (.build (options/build ArangoDB$Builder options))))

(defn ^Boolean create-database
  "returns `true` on success else `ArangoDBException`"
  [^ArangoDB conn ^String db-name]
  (.createDatabase conn db-name))

(defn ^ArangoDatabase db
  "Always returns a new `ArrangoDatabase` even if no such database exists
  the returned object can be used if a databse is created at a later time"
  [^ArangoDB conn ^String db-name]
  (.db conn db-name))

(def get-database db)

(defn ^Boolean create-and-get-database
  ""
  [^ArangoDB conn ^String db-name]
  (do (.createDatabase conn db-name)
      (.db conn db-name)))

(defn get-databases
  "returns a `vec` of strings corresponding to the names of databases"
  [^ArangoDB conn] (vec (.getDatabases conn)))

(defn database?
  "returns true if `db-name` is an existsing db"
  [^ArangoDB conn ^String db-name]
  (boolean (some #{db-name} (get-databases conn))))
