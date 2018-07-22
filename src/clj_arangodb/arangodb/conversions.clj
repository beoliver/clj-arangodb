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

(defmulti ->map class)

(defmethod ->map :default
  [o] (bean-no-class o))

;;; make sure that `->map` is idempotent wrt clojure maps and strings

(defmethod ->map PersistentArrayMap
  [o] o)
(defmethod ->map PersistentHashMap
  [o] o)
(defmethod ->map String
  [o] o)

(defmethod ->map VPackSlice
  [o]
  (vpack/unpack o keyword))

(defmethod ->map MultiDocumentEntity
  [o] (-> o
          bean
          (update-many [:documents :errors :documentsAndErrors] #(map ->map %))))

(defmethod ->map CollectionEntity
  [o] (-> o
          bean
          (update-many [:status :type] str)))

(defmethod ->map GraphEntity
  [o] (-> o
          bean
          (update :edgeDefinitions #(into [] (map ->map %)))))
