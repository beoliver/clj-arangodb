(ns clj-arangodb.arangodb.collections
  (:require [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.conversions :as conv]
            [clj-arangodb.arangodb.options :as options]
            [clj-arangodb.arangodb.utils :refer [maybe-vpack MultiDocumentEntity->map]])
  (:import
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.velocypack.VPackSlice
   com.arangodb.entity.BaseDocument
   com.arangodb.entity.BaseEdgeDocument
   [com.arangodb.model
    DocumentCreateOptions
    DocumentReadOptions
    DocumentUpdateOptions
    DocumentDeleteOptions
    DocumentReplaceOptions
    ])
  (:refer-clojure :exclude [drop]))

(defn ^CollectionEntity rename [^ArangoCollection coll ^String new-name]
  (.rename coll new-name))

(defn get-document
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  "
  ([^ArangoCollection coll ^String key]
   (vpack/unpack (.getDocument coll key VPackSlice) keyword))
  ([^ArangoCollection coll ^String key ^Class as]
   (.getDocument coll key as))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (.getDocument coll key as (options/build DocumentReadOptions options))))

(defn insert-document
  ([^ArangoCollection coll ^Object doc]
   (.insertDocument coll (maybe-vpack doc)))
  ([^ArangoCollection coll ^Object doc ^DocumentCreateOptions options]
   (.insertDocument coll (maybe-vpack doc)
                    (options/build DocumentCreateOptions options))))

(defn insert-documents
  ([^ArangoCollection coll docs]
   (.insertDocuments coll (java.util.ArrayList. (map maybe-vpack docs))))
  ([^ArangoCollection coll docs ^DocumentCreateOptions options]
   (.insertDocuments coll (java.util.ArrayList. (map maybe-vpack docs))
                     (options/build DocumentCreateOptions options))))

(defn update-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (.updateDocument coll key (maybe-vpack doc)))
  ([^ArangoCollection coll ^String key doc ^DocumentUpdateOptions options]
   (.updateDocument coll key (maybe-vpack doc)
                    (options/build DocumentUpdateOptions options))))

(defn update-documents
  ([^ArangoCollection coll docs]
   (.updateDocuments coll docs))
  ([^ArangoCollection coll docs ^DocumentUpdateOptions options]
   (.updateDocuments coll (java.util.ArrayList. (map maybe-vpack docs))
                     (options/build DocumentUpdateOptions options))))

(defn replace-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (.replaceDocument coll key doc))
  ([^ArangoCollection coll ^String key ^Object doc ^DocumentReplaceOptions options]
   (.replaceDocument coll key doc (options/build DocumentReplaceOptions options))))

(defn replace-documents
  ([^ArangoCollection coll docs]
   (.replaceDocuments coll (java.util.ArrayList. (map maybe-vpack docs))))
  ([^ArangoCollection coll docs ^DocumentReplaceOptions options]
   (.replaceDocuments coll (java.util.ArrayList. (map maybe-vpack docs))
                      (options/build DocumentReplaceOptions options))))

(defn delete-document
  ([^ArangoCollection coll ^String key]
   (.deleteDocument coll key))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentDeleteOptions options]
   (.deleteDocument coll key as (options/build DocumentDeleteOptions options))))

(defn delete-documents
  ([^ArangoCollection coll keys]
   (.deleteDocuments coll keys))
  ([^ArangoCollection coll keys ^Class as ^DocumentDeleteOptions options]
   (.deleteDocuments coll (java.util.ArrayList. keys) as
                     (options/build DocumentDeleteOptions options))))

(defn truncate [^ArangoCollection coll]
  (.truncate coll))

(defn drop
  ([^ArangoCollection coll] (.drop coll))
  ([^ArangoCollection coll ^Boolean flag] (.drop coll flag)))
