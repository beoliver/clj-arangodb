(ns clj-arangodb.arangodb.conversions
  (:require [clj-arangodb.velocypack.core :as vpack])
  (:import [clojure.lang
            PersistentArrayMap
            PersistentHashMap]
           com.arangodb.velocypack.VPackSlice
           [com.arangodb.entity
            CollectionEntity
            GraphEntity
            EdgeDefinition
            DocumentCreateEntity
            MultiDocumentEntity
            DocumentUpdateEntity
            DocumentDeleteEntity]))

(defn bean-no-class [o]
  (-> o
      bean
      (dissoc :class)))

(defn update-many [m keys f]
  (reduce (fn [m k] (update m k f)) m keys))

(defmulti ->result class)

(defmethod ->result :default
  [o] o)

;;; make sure that `->result` is idempotent wrt clojure maps and strings

(defmethod ->result PersistentArrayMap
  [o] o)
(defmethod ->result PersistentHashMap
  [o] o)
(defmethod ->result String
  [o] o)

(defmethod ->result VPackSlice
  [o]
  (vpack/unpack o keyword))

(defmethod ->result MultiDocumentEntity
  [o] (-> o
          bean
          (update-many [:documents :errors :documentsAndErrors] #(map ->result %))))

(defmethod ->result CollectionEntity
  [o] (-> o
          bean
          (update-many [:status :type] str)))

(defmethod ->result GraphEntity
  [o] (-> o
          bean
          (update :edgeDefinitions #(into [] (map ->result %)))))
