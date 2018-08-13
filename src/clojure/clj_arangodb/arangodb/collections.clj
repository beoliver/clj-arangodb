(ns clj-arangodb.arangodb.collections
  (:require [clj-arangodb.arangodb.adapter :as ad]
            [clj-arangodb.arangodb.options :as options])
  (:import [com.arangodb
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

(defn ^CollectionEntity get-info [^ArangoCollection coll]
  (ad/from-entity (.getInfo coll)))

(defn ^CollectionPropertiesEntity get-properties [^ArangoCollection coll]
  (ad/from-entity (.getProperties coll)))

(defn ^CollectionRevisionEntity get-revision [^ArangoCollection coll]
  (ad/from-entity (.getRevision coll)))

(defn ^Boolean exists? [^ArangoCollection coll]
  (.exists coll))

(defn ^CollectionEntity rename [^ArangoCollection coll ^String new-name]
  (ad/from-entity (.rename coll new-name)))

(defn ^CollectionEntity load [^ArangoCollection coll]
  (ad/from-entity (.load coll)))

(defn ^CollectionEntity unload [^ArangoCollection coll]
  (ad/from-entity (.unload coll)))

(defn ^CollectionEntity change-properties
  [^ArangoCollection coll ^CollectionPropertiesOptions options]
  (ad/from-entity (.changeProperties coll (options/build CollectionPropertiesOptions options))))

(defn ^CollectionEntity truncate [^ArangoCollection coll]
  (ad/from-entity (.tuncate coll)))

(defn drop ;;void
  ([^ArangoCollection coll] (.drop coll))
  ([^ArangoCollection coll ^Boolean flag] (.drop coll flag)))

(defn ^IndexEntity ensure-hash-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^HashIndexOptions options]
  (ad/from-entity (.ensureHashIndex coll fields (options/build HashIndexOptions options))))

(defn ^IndexEntity ensure-skip-list-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^SkiplistIndexOptions options]
  (ad/from-entity (.ensureSkiplistIndex coll fields (options/build SkiplistIndexOptions options))))

(defn ^IndexEntity ensure-geo-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^GeoIndexOptions options]
  (ad/from-entity (.ensureGeoIndex coll fields (options/build GeoIndexOptions options))))

(defn ^IndexEntity ensure-full-text-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^FulltextIndexOptions options]
  (ad/from-entity (.ensureFulltextIndex coll fields (options/build FulltextIndexOptions options))))

(defn ^IndexEntity ensure-persistent-index
  [^ArangoCollection coll ^java.lang.Iterable fields ^PersistentIndexOptions options]
  (ad/from-entity (.ensurePersistentIndex coll fields (options/build PersistentIndexOptions options))))

(defn ^IndexEntity get-index
  [^ArangoCollection coll ^String index]
  (ad/from-entity (.getIndex coll index)))

(defn ^java.util.Collection get-indexes
  ;; collection of IndexEntity
  [^ArangoCollection coll]
  (map ad/from-entity (.getIndexes coll)))

(defn ^String delete-index
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

(defn ^MultiDocumentEntity get-documents
  "
   Class represents the class of the returned document.
  `String` will return a json encoding
  `VpackSlice` will return a arangodb velocypack slice
  `BaseDocument` will return a java object
  `Map` will return a java map
  "
  ([^ArangoCollection coll keys]
   (get-documents coll keys ad/*default-doc-class*))
  ([^ArangoCollection coll keys ^Class as]
   (ad/from-entity (.getDocuments coll (java.util.ArrayList. keys) as))))

(defn ^DocumentCreateEntity insert-document
  ([^ArangoCollection coll ^Object doc]
   (ad/from-entity (.insertDocument coll (ad/serialize-doc doc))))
  ([^ArangoCollection coll ^Object doc ^DocumentCreateOptions options]
   (ad/from-entity (.insertDocument coll (ad/serialize-doc doc)
                                    (options/build DocumentCreateOptions options)))))

(defn ^MultiDocumentEntity insert-documents
  ([^ArangoCollection coll docs]
   (ad/from-entity (.insertDocuments coll (java.util.ArrayList.
                                           (map ad/serialize-doc docs)))))
  ([^ArangoCollection coll docs ^DocumentCreateOptions options]
   (ad/from-entity (.insertDocuments coll (java.util.ArrayList. (map ad/serialize-doc docs))
                                     (options/build DocumentCreateOptions options)))))

(defn ^DocumentImportEntity import-documents
  ([^ArangoCollection coll docs]
   (ad/from-entity (.importDocuments coll docs)))
  ([^ArangoCollection coll docs ^DocumentImportOptions options]
   (ad/from-entity (.importDocuments coll docs (options/build DocumentImportOptions options)))))

(defn ^DocumentUpdateEntity update-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (ad/from-entity (.updateDocument coll key (ad/serialize-doc doc))))
  ([^ArangoCollection coll ^String key doc ^DocumentUpdateOptions options]
   (ad/from-entity (.updateDocument coll key (ad/serialize-doc doc)
                                    (options/build DocumentUpdateOptions options)))))

(defn ^MultiDocumentEntity update-documents
  ([^ArangoCollection coll docs]
   (ad/from-entity (.updateDocuments coll (java.util.ArrayList. (map ad/serialize-doc docs)))))
  ([^ArangoCollection coll docs ^DocumentUpdateOptions options]
   (ad/from-entity (.updateDocuments coll (java.util.ArrayList. (map ad/serialize-doc docs))
                                     (options/build DocumentUpdateOptions options)))))

(defn ^DocumentUpdateEntity replace-document
  ([^ArangoCollection coll ^String key ^Object doc]
   (ad/from-entity (.replaceDocument coll key doc)))
  ([^ArangoCollection coll ^String key ^Object doc ^DocumentReplaceOptions options]
   (ad/from-entity (.replaceDocument coll key doc (options/build DocumentReplaceOptions options)))))

(defn ^MultiDocumentEntity replace-documents
  ([^ArangoCollection coll docs]
   (ad/from-entity (.replaceDocuments coll (java.util.ArrayList. (map ad/serialize-doc docs)))))
  ([^ArangoCollection coll docs ^DocumentReplaceOptions options]
   (ad/from-entity (.replaceDocuments coll (java.util.ArrayList. (map ad/serialize-doc docs))
                                      (options/build DocumentReplaceOptions options)))))

(defn ^DocumentDeleteEntity delete-document
  ([^ArangoCollection coll ^String key]
   (ad/from-entity (.deleteDocument coll key)))
  ([^ArangoCollection coll ^String key ^Class as ^DocumentDeleteOptions options]
   (ad/from-entity (.deleteDocument coll key as (options/build DocumentDeleteOptions options)))))

(defn ^MultiDocumentEntity delete-documents
  ([^ArangoCollection coll keys]
   (ad/from-entity (.deleteDocuments coll (java.util.ArrayList. keys))))
  ([^ArangoCollection coll keys ^Class as ^DocumentDeleteOptions options]
   (ad/from-entity (.deleteDocuments coll (java.util.ArrayList. keys) as
                                     (options/build DocumentDeleteOptions options)))))
