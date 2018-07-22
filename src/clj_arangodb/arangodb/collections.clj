(ns clj-arangodb.arangodb.collections
  (:require [clj-arangodb.velocypack.core :as vpack]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.adapter :refer
             [serialize-doc deserialize-doc from-entity]]
            [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb
            ArangoCollection]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            IndexEntity
            BaseDocument
            BaseEdgeDocument
            CollectionEntity
            MultiDocumentEntity
            DocumentCreateEntity
            MultiDocumentEntity
            DocumentUpdateEntity
            DocumentDeleteEntity
            DocumentImportEntity]
           [com.arangodb.model
            CollectionPropertiesOptions
            SkiplistIndexOptions
            GeoIndexOptions
            FulltextIndexOptions
            PersistentIndexOptions
            HashIndexOptions
            DocumentCreateOptions
            DocumentReadOptions
            DocumentUpdateOptions
            DocumentDeleteOptions
            DocumentReplaceOptions
            DocumentImportOptions])
  (:refer-clojure :exclude [drop]))

(defn ^CollectionEntity rename [^ArangoCollection coll ^String new-name]
  (from-entity (.rename coll new-name)))

(defn ^CollectionEntity load [^ArangoCollection coll]
  (from-entity (.load coll)))

(defn ^CollectionEntity unload [^ArangoCollection coll]
  (from-entity (.unload coll)))

(defn ^CollectionEntity change-properties
  [^ArangoCollection coll ^CollectionPropertiesOptions options]
  (from-entity (.changeProperties coll (options/build CollectionPropertiesOptions options))))

(defn ^CollectionEntity truncate [^ArangoCollection coll]
  (from-entity (.tuncate coll)))

(defn ^IndexEntity ensure-hash-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^HashIndexOptions options]
  (from-entity (.ensureHashIndex coll fields (options/build HashIndexOptions options))))

(defn ^IndexEntity ensure-skip-list-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^SkiplistIndexOptions options]
  (from-entity (.ensureSkiplistIndex coll fields (options/build SkiplistIndexOptions options))))

(defn ^IndexEntity ensure-geo-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^GeoIndexOptions options]
  (from-entity (.ensureGeoIndex coll fields (options/build GeoIndexOptions options))))

(defn ^IndexEntity ensure-full-text-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^FulltextIndexOptions options]
  (from-entity (.ensureFulltextIndex coll fields (options/build FulltextIndexOptions options))))

(defn ^IndexEntity ensure-persisten-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^PersistentIndexOptions options]
  (from-entity (.ensurePersistentIndex coll fields (options/build PersistentIndexOptions options))))

(defn ^IndexEntity get-index
  [^ArangoCollection coll ^String index]
  (from-entity (.getIndex coll index)))

(defn ^java.util.Collection get-indexes
  [^ArangoCollection coll]
  (map from-entity (.getIndexes coll)))

(defn ^String delete-index
  [^ArangoCollection coll ^String index]
  (.deleteIndex coll index))


(defn get-document
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  "
  ([^ArangoCollection coll ^String key]
   (get-document coll key VPackSlice))
  ([^ArangoCollection coll ^String key ^Class as]
   (deserialize-doc (.getDocument coll key as)))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (deserialize-doc (.getDocument coll key as (options/build DocumentReadOptions options)))))

(defn ^MultiDocumentEntity get-documents
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  "
  ([^ArangoCollection coll keys]
   (get-documents coll keys VPackSlice))
  ([^ArangoCollection coll keys ^Class as]
   (from-entity (.getDocuments coll (java.util.ArrayList. keys) as))))

(defn ^DocumentCreateEntity insert-document
  ([^ArangoCollection coll ^Object doc]
   (from-entity (.insertDocument coll (serialize-doc doc))))
  ([^ArangoCollection coll ^Object doc ^DocumentCreateOptions options]
   (from-entity (.insertDocument coll (serialize-doc doc)
                                 (options/build DocumentCreateOptions options)))))

(defn ^MultiDocumentEntity insert-documents
  ([^ArangoCollection coll docs]
   (from-entity (.insertDocuments coll (java.util.ArrayList. (map serialize-doc docs)))))
  ([^ArangoCollection coll docs ^DocumentCreateOptions options]
   (from-entity (.insertDocuments coll (java.util.ArrayList. (map serialize-doc docs))
                                  (options/build DocumentCreateOptions options)))))

(defn ^DocumentImportEntity import-documents
  ([^ArangoCollection coll docs]
   (from-entity (.importDocuments coll docs)))
  ([^ArangoCollection coll docs ^DocumentImportOptions options]
   (from-entity (.importDocuments coll docs (options/build DocumentImportOptions options)))))

(defn ^DocumentUpdateEntity update-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (from-entity (.updateDocument coll key (serialize-doc doc))))
  ([^ArangoCollection coll ^String key doc ^DocumentUpdateOptions options]
   (from-entity (.updateDocument coll key (serialize-doc doc)
                                 (options/build DocumentUpdateOptions options)))))

(defn ^MultiDocumentEntity update-documents
  ([^ArangoCollection coll docs]
   (from-entity (.updateDocuments coll (java.util.ArrayList. (map serialize-doc docs)))))
  ([^ArangoCollection coll docs ^DocumentUpdateOptions options]
   (from-entity (.updateDocuments coll (java.util.ArrayList. (map serialize-doc docs))
                                  (options/build DocumentUpdateOptions options)))))

(defn ^DocumentUpdateEntity replace-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (from-entity (.replaceDocument coll key doc)))
  ([^ArangoCollection coll ^String key ^Object doc ^DocumentReplaceOptions options]
   (from-entity (.replaceDocument coll key doc (options/build DocumentReplaceOptions options)))))

(defn ^MultiDocumentEntity replace-documents
  ([^ArangoCollection coll docs]
   (from-entity (.replaceDocuments coll (java.util.ArrayList. (map serialize-doc docs)))))
  ([^ArangoCollection coll docs ^DocumentReplaceOptions options]
   (from-entity (.replaceDocuments coll (java.util.ArrayList. (map serialize-doc docs))
                                   (options/build DocumentReplaceOptions options)))))

(defn ^DocumentDeleteEntity delete-document
  ([^ArangoCollection coll ^String key]
   (from-entity (.deleteDocument coll key)))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentDeleteOptions options]
   (from-entity (.deleteDocument coll key as (options/build DocumentDeleteOptions options)))))

(defn ^MultiDocumentEntity delete-documents
  ([^ArangoCollection coll keys]
   (from-entity (.deleteDocuments coll (java.util.ArrayList. keys))))
  ([^ArangoCollection coll keys ^Class as ^DocumentDeleteOptions options]
   (from-entity (.deleteDocuments coll (java.util.ArrayList. keys) as
                                  (options/build DocumentDeleteOptions options)))))

(defn truncate [^ArangoCollection coll]
  (.truncate coll))

(defn drop
  ([^ArangoCollection coll] (.drop coll))
  ([^ArangoCollection coll ^Boolean flag] (.drop coll flag)))
