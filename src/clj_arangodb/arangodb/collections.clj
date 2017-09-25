(ns clj-arangodb.arangodb.collections
  (:import com.arangodb.velocypack.VPackSlice))

;;; collections
;; (isSystem getName getIsSystem name getStatus id isVolatile getWaitForSync waitForSync status com.arangodb.entity.CollectionEntity getType getIsVolatile getId type)

(defn get-status [coll]
  (str (.getStatus coll)))

(defn get-name [coll]
  (.getName coll))

(defn get-type [coll]
  (str (.getType coll)))

(defn get-id [coll]
  (.getId coll))

(defn insert-document
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with the `:id` `:key` `:new` and `:rev`"
  [coll doc]
  (-> (.insertDocument coll doc) bean (dissoc :class)))

(defn insert-documents
  [coll docs]
  (.insertDocuments coll (java.util.ArrayList. docs)))

(defn update-document
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String)."
  [coll key doc]
  (.updateDocument coll key doc))

(defn update-documents
  [coll docs]
  (.updateDocuments coll (java.util.ArrayList. docs)))

(defn replace-document
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String)."
  [coll key doc]
  (.replaceDocument coll key doc))

(defn replace-documents
  [coll docs]
  (.replaceDocuments coll (java.util.ArrayList. docs)))

(defn delete-document
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String)."
  [coll key]
  (.deleteDocument coll key))

(defn delete-documents
  [coll keys]
  (.deleteDocument coll (java.util.ArrayList. keys)))

(defn get-document-as-vpack
  [coll key]
  (.getDocument coll key VPackSlice))

(defn get-document-as-json
  [coll key]
  (.getDocument coll key String))

(defn get-document-as-java-bean
  [coll key class]
  (.getDocument coll key class))
