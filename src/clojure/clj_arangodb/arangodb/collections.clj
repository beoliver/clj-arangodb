(ns clj-arangodb.arangodb.collections
  (:require [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.options :as options])
  (:import [java.util
            ArrayList
            Collection]
           [com.arangodb
            ArangoCollection]
           [com.arangodb.entity
            CollectionEntity
            IndexEntity
            MultiDocumentEntity
            DocumentCreateEntity
            DocumentUpdateEntity
            DocumentDeleteEntity
            DocumentImportEntity
            CollectionPropertiesEntity
            CollectionRevisionEntity]
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
  (:refer-clojure :exclude [drop load]))

(defn get-info ^CollectionEntity
  [^ArangoCollection coll]
  (ad/from-entity (.getInfo coll)))

(defn get-properties ^CollectionPropertiesEntity
  [^ArangoCollection coll]
  (ad/from-entity (.getProperties coll)))

(defn get-revision ^CollectionRevisionEntity
  [^ArangoCollection coll]
  (ad/from-entity (.getRevision coll)))

(defn exists? ^Boolean
  [^ArangoCollection coll]
  (.exists coll))

(defn rename ^CollectionEntity
  [^ArangoCollection coll ^String new-name]
  (ad/from-entity (.rename coll new-name)))

(defn load ^CollectionEntity
  [^ArangoCollection coll]
  (ad/from-entity (.load coll)))

(defn unload ^CollectionEntity
  [^ArangoCollection coll]
  (ad/from-entity (.unload coll)))

(defn change-properties
  ^CollectionEntity
  [^ArangoCollection coll ^CollectionPropertiesOptions options]
  (ad/from-entity (.changeProperties coll (options/build CollectionPropertiesOptions options))))

;; (defn truncate ^CollectionEntity
;;   [^ArangoCollection coll]
;;   (ad/from-entity (.tuncate coll)))

(defn drop ;;void
  ([^ArangoCollection coll] (.drop coll))
  ([^ArangoCollection coll ^Boolean flag] (.drop coll flag)))

(defn ensure-hash-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^HashIndexOptions options]
  (ad/from-entity (.ensureHashIndex coll fields (options/build HashIndexOptions options))))

(defn ^IndexEntity ensure-skip-list-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^SkiplistIndexOptions options]
  (ad/from-entity (.ensureSkiplistIndex coll fields (options/build SkiplistIndexOptions options))))

(defn ensure-geo-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^GeoIndexOptions options]
  (ad/from-entity (.ensureGeoIndex coll fields (options/build GeoIndexOptions options))))

(defn ensure-full-text-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^FulltextIndexOptions options]
  (ad/from-entity (.ensureFulltextIndex coll fields (options/build FulltextIndexOptions options))))

(defn ensure-persistent-index ^IndexEntity
  [^ArangoCollection coll ^java.lang.Iterable fields ^PersistentIndexOptions options]
  (ad/from-entity (.ensurePersistentIndex coll fields (options/build PersistentIndexOptions options))))

(defn get-index ^IndexEntity
  [^ArangoCollection coll ^String index]
  (ad/from-entity (.getIndex coll index)))

(defn get-indexes ^java.util.Collection
  ;; collection of IndexEntity
  [^ArangoCollection coll]
  (map ad/from-entity (.getIndexes coll)))

(defn delete-index ^String
  [^ArangoCollection coll ^String index]
  (.deleteIndex coll index))

(defn get-document
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  `Map` will return a java map
  "
  ([^ArangoCollection coll ^String key]
   (get-document coll key ad/*default-doc-class*))
  ([^ArangoCollection coll ^String key ^Class as]
   (ad/deserialize-doc (.getDocument coll key as)))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentReadOptions options]
   (ad/deserialize-doc (.getDocument coll key as (options/build DocumentReadOptions options)))))

(defn get-documents
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  `Map` will return a java map
  "
  ^MultiDocumentEntity
  ([^ArangoCollection coll ^Collection keys]
   (get-documents coll keys ad/*default-doc-class*))
  ([^ArangoCollection coll ^Collection keys ^Class as]
   (ad/from-entity (.getDocuments coll keys as))))

(defn insert-document ^DocumentCreateEntity
  ([^ArangoCollection coll ^Object doc]
   (ad/from-entity (.insertDocument coll (ad/serialize-doc doc))))
  ([^ArangoCollection coll ^Object doc ^DocumentCreateOptions options]
   (ad/from-entity (.insertDocument coll (ad/serialize-doc doc)
                                    (options/build DocumentCreateOptions options)))))

(defn insert-documents ^MultiDocumentEntity
  ([^ArangoCollection coll docs]
   (ad/from-entity (.insertDocuments coll ^Collection (map ad/serialize-doc docs))))
  ([^ArangoCollection coll docs ^DocumentCreateOptions options]
   (ad/from-entity (.insertDocuments coll ^Collection (map ad/serialize-doc docs)
                                     (options/build DocumentCreateOptions options)))))

;; (defn import-documents ^DocumentImportEntity
;;   ([^ArangoCollection coll ^Collection docs]
;;    (ad/from-entity (.importDocuments coll docs)))
;;   ([^ArangoCollection coll ^Collection docs ^DocumentImportOptions options]
;;    (ad/from-entity (.importDocuments coll docs (options/build DocumentImportOptions options)))))

(defn update-document ^DocumentUpdateEntity
  ([^ArangoCollection coll ^String key ^Object doc]
   (ad/from-entity (.updateDocument coll key (ad/serialize-doc doc))))
  ([^ArangoCollection coll ^String key doc ^DocumentUpdateOptions options]
   (ad/from-entity (.updateDocument coll key (ad/serialize-doc doc)
                                    (options/build DocumentUpdateOptions options)))))

(defn update-documents ^MultiDocumentEntity
  ([^ArangoCollection coll docs]
   (ad/from-entity (.updateDocuments coll ^Collection (map ad/serialize-doc docs))))
  ([^ArangoCollection coll docs ^DocumentUpdateOptions options]
   (ad/from-entity (.updateDocuments coll ^Collection (map ad/serialize-doc docs)
                                     (options/build DocumentUpdateOptions options)))))

(defn replace-document ^DocumentUpdateEntity
  ([^ArangoCollection coll ^String key ^Object doc]
   (ad/from-entity (.replaceDocument coll key doc)))
  ([^ArangoCollection coll ^String key ^Object doc ^DocumentReplaceOptions options]
   (ad/from-entity (.replaceDocument coll key doc (options/build DocumentReplaceOptions options)))))

(defn replace-documents ^MultiDocumentEntity
  ([^ArangoCollection coll docs]
   (ad/from-entity (.replaceDocuments coll ^Collection (map ad/serialize-doc docs))))
  ([^ArangoCollection coll docs ^DocumentReplaceOptions options]
   (ad/from-entity (.replaceDocuments coll ^Collection (map ad/serialize-doc docs)
                                      (options/build DocumentReplaceOptions options)))))

(defn delete-document ^DocumentDeleteEntity
  ([^ArangoCollection coll ^String key]
   (ad/from-entity (.deleteDocument coll key)))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentDeleteOptions options]
   (ad/from-entity (.deleteDocument coll key as (options/build DocumentDeleteOptions options)))))

(defn delete-documents ^MultiDocumentEntity
  ([^ArangoCollection coll ^Collection keys]
   (ad/from-entity (.deleteDocuments coll keys)))
  ([^ArangoCollection coll ^Collection keys ^Class as ^DocumentDeleteOptions options]
   (ad/from-entity (.deleteDocuments coll keys as
                                     (options/build DocumentDeleteOptions options)))))
