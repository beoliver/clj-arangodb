(ns clj-arangodb.arangodb.core
  (:require [clj-arangodb.arangodb.builder :refer [build-new-arango-db]]))

(defn new-arrangodb
  [{:keys [host port user password] :as options}]
  (build-new-arango-db options))

(defn create-database
  [conn name]
  (.createDatabase conn name))

(defn drop-database
  "returns `true` if database with `name` was dropped"
  [conn name]
  (-> conn (.db name) .drop))

(defn get-database
  "returns a new `ArrangoDatabase` even if `name` does not exist."
  [conn name]
  (-> conn (.db name)))
