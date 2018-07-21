(ns clj-arangodb.arangodb.collections
  (:require [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.utils :refer [maybe-vpack MultiDocumentEntity->map]])
  (:import
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.velocypack.VPackSlice
   com.arangodb.model.DocumentCreateOptions
   com.arangodb.model.DocumentReadOptions
   com.arangodb.model.DocumentReplaceOptions
   com.arangodb.model.DocumentUpdateOptions
   com.arangodb.model.DocumentDeleteOptions)
  (:refer-clojure :exclude [drop]))

(defn ^CollectionEntity rename [^ArangoCollection coll ^String new-name]
  (.rename coll new-name))

(defn get-document-as-map
  ([^ArangoCollection coll key]
   (vpack/unpack (.getDocument coll key VPackSlice) keyword))
  ([^ArangoCollection coll key key-fn]
   (vpack/unpack (.getDocument coll key VPackSlice) key-fn)))

(defn get-document
  ([^ArangoCollection coll ^String key ^Class as]
   (.getDocument coll key as))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (.getDocument coll key as options)))

(defn insert-document
  ([^ArangoCollection coll ^Object doc]
   (.insertDocument coll doc))
  ([^ArangoCollection coll ^Object doc ^DocumentCreateOptions options]
   (.insertDocument coll doc options)))

(defn insert-documents
  ([^ArangoCollection coll docs]
   (.insertDocuments coll docs))
  ([^ArangoCollection coll docs ^DocumentCreateOptions options]
   (.insertDocuments coll (java.util.ArrayList. docs) options)))

(defn update-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (.updateDocument coll key doc))
  ([^ArangoCollection coll ^String key doc ^DocumentUpdateOptions options]
   (.updateDocument coll key doc options)))

(defn update-documents
  ([^ArangoCollection coll docs]
   (.updateDocuments coll docs))
  ([^ArangoCollection coll docs ^DocumentUpdateOptions options]
   (.updateDocuments coll (java.util.ArrayList. docs) options)))

(defn replace-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (.replaceDocument coll key doc))
  ([^ArangoCollection coll ^String key ^Object doc ^DocumentReplaceOptions options]
   (.replaceDocument coll key doc options)))

(defn replace-documents
  ([^ArangoCollection coll docs]
   (.replaceDocuments coll (java.util.ArrayList. docs)))
  ([^ArangoCollection coll docs ^DocumentReplaceOptions options]
   (.replaceDocuments coll (java.util.ArrayList. docs) options)))

(defn delete-document
  ([^ArangoCollection coll ^String key]
   (.deleteDocument coll key))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentDeleteOptions options]
   (.deleteDocument coll key as options)))

(defn delete-documents
  ([^ArangoCollection coll keys]
   (.deleteDocuments coll keys))
  ([^ArangoCollection coll keys ^Class as ^DocumentDeleteOptions options]
   (.deleteDocuments coll (java.util.ArrayList. keys) as options)))

(defn truncate [^ArangoCollection coll]
  (.truncate coll))

(defn drop
  ([^ArangoCollection coll] (.drop coll))
  ([^ArangoCollection coll ^Boolean flag] (.drop coll flag)))
