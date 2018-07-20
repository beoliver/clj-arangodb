(ns clj-arangodb.arangodb.graph
  (:require
   [clj-arangodb.velocypack.core :as v]
   [pjson.core :as json]
   [clojure.set :as set]
   [clojure.reflect :as r])
  (:import com.arangodb.entity.EdgeDefinition
           com.arangodb.ArangoGraph
           com.arangodb.velocypack.VPackSlice))

(defn make-multigraph [edge-collection-name edges]
  ;; an edge is of the from ["source" ["target_1" ... "target_n"]]
  (reduce (fn [edge-def [source targets]]
            (reduce (fn [edge-def target]
                      (.from edge-def source target)) edge-def targets))
          (new EdgeDefinition) edges))

;; (map :name (:members (r/reflect conn)))

(defn get-info [^ArangoGraph x] (bean (.getInfo x)))

(defn get-members [x] (map :name (:members (r/reflect x))))

(defn get-vertex-collections [^ArangoGraph graph]
  (.getVertexCollections graph))

(defn get-edge-definitions [^ArangoGraph graph]
  (.getEdgeDefinitions graph))

(defn edge-collection "get the actual collection" [^ArangoGraph graph coll-name]
  (.edgeCollection graph coll-name))

(defn vertex-collection "get the actual collection" [^ArangoGraph graph coll-name]
  (.vertexCollection graph coll-name))

(defn add-vertex-collection [^ArangoGraph graph ^String collection-name]
  ;; returns ArangoDBException Response: 400, Error: 1938 - collection used in orphans if
  ;; you try adding the collection twice
  ;; arangoDB.db("myDatabase").graph("myGraph").drop();
  (.addVertexCollection graph collection-name))

(defn edge-definition
  [{:keys [name from to] :as edge-definition}]
  (-> (new EdgeDefinition)
      (.collection name)
      (.from (into-array from))
      (.to (into-array to))))
