(ns clj-arangodb.arangodb.collections
  (:require [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.collection-options :as options]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.graph :as g]
            [clj-arangodb.arangodb.utils :refer [maybe-vpack MultiDocumentEntity->map]])
  (:import
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.ArangoEdgeCollection
   com.arangodb.ArangoVertexCollection
   com.arangodb.velocypack.VPackSlice)
  (:refer-clojure :exclude [drop]))

(defn ^CollectionEntity rename [^ArangoCollection coll new-name]
  (.rename coll new-name))

(defn get-map
  ([^ArangoCollection coll key]
   (vpack/unpack (.getDocument coll key VPackSlice) keyword))
  ([^ArangoCollection coll key key-fn]
   (vpack/unpack (.getDocument coll key VPackSlice) key-fn)))

(defn get-document
  [^ArangoCollection coll key ^Class as]
  (.getDocument coll key as))

(defn insert-document
  ([^ArangoCollection coll doc]
   (insert-document coll doc bean))
  ([^ArangoCollection coll doc post-fn]
   (post-fn (.insertDocument coll (maybe-vpack doc)))))

(defn insert-documents
  ([^ArangoCollection coll docs]
   (insert-documents coll docs MultiDocumentEntity->map))
  ([^ArangoCollection coll docs post-fn]
   (post-fn (.insertDocuments coll (java.util.ArrayList. (map maybe-vpack docs))))))

(defn update-document
  ([^ArangoCollection coll key doc]
   (update-document coll key doc bean))
  ([^ArangoCollection coll key doc post-fn]
   (post-fn (.updateDocument coll key (maybe-vpack doc)))))

(defn update-documents
  ([^ArangoCollection coll docs]
   (update-documents coll docs MultiDocumentEntity->map))
  ([^ArangoCollection coll docs post-fn]
   (post-fn (.updateDocuments coll (java.util.ArrayList. (map maybe-vpack docs))))))

(defn replace-document
  ([^ArangoCollection coll doc]
   (replace-document coll doc bean))
  ([^ArangoCollection coll doc post-fn]
   (post-fn (.replaceDocument coll (maybe-vpack doc)))))

(defn replace-documents
  ([^ArangoCollection coll docs]
   (replace-documents coll docs MultiDocumentEntity->map))
  ([^ArangoCollection coll docs post-fn]
   (post-fn (.replaceDocuments coll (java.util.ArrayList. (map maybe-vpack docs))))))

(defn delete-document
  ([^ArangoCollection coll doc]
   (delete-document coll doc bean))
  ([^ArangoCollection coll doc post-fn]
   (post-fn (.deleteDocument coll (maybe-vpack doc)))))

(defn delete-documents
  ([^ArangoCollection coll docs]
   (delete-documents coll docs MultiDocumentEntity->map))
  ([^ArangoCollection coll docs post-fn]
   (post-fn (.deleteDocuments coll (java.util.ArrayList. (map maybe-vpack docs))))))

(defn truncate [^ArangoCollection coll]
  (.truncate coll))

(defn drop [^ArangoCollection coll]
  (.drop coll))

(defn get-edge
  [^ArangoEdgeCollection coll key ^Class as]
  (.getEdge coll key as))

(defn insert-edge
  ([^ArangoEdgeCollection coll {:keys [_from _to] :as doc}]
   (insert-edge coll doc bean))
  ([^ArangoEdgeCollection coll doc post-fn]
   (post-fn (.insertEdge coll (maybe-vpack doc)))))

(defn insert-edges
  ([^ArangoEdgeCollection coll docs]
   (insert-edges coll docs MultiDocumentEntity->map))
  ([^ArangoEdgeCollection coll docs post-fn]
   (post-fn (.insertEdges coll (java.util.ArrayList. (map maybe-vpack docs))))))

(defn get-vertex
  [^ArangoVertexCollection coll key ^Class as]
  (.getVertex coll key as))

(defn insert-vertex
  ([^ArangoVertexCollection coll doc]
   (insert-vertex coll doc bean))
  ([^ArangoEdgeCollection coll doc post-fn]
   (post-fn (.insertVertex coll (maybe-vpack doc)))))
