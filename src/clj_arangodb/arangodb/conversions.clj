(ns clj-arangodb.arangodb.conversions
  (:import [clojure.lang
            PersistentArrayMap
            PersistentHashMap]
           [com.arangodb.entity
            CollectionEntity
            GraphEntity
            EdgeDefinition
            DocumentCreateEntity
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

;;; make sure that `->map` is idempotent wrt clojure maps

(defmethod ->map PersistentArrayMap
  [o] o)
(defmethod ->map PersistentHashMap
  [o] o)

(defmethod ->map CollectionEntity
  [o] (-> o
          bean-no-class
          (update-many [:status :type] str)))

(defmethod ->map GraphEntity
  [o] (-> o
          bean-no-class
          (update :edgeDefinitions #(into [] (map ->map %)))))
