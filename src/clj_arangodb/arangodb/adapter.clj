(ns clj-arangodb.arangodb.adapter
  (:require [clj-arangodb.velocypack.core :as vpack])
  (:import [clojure.lang
            PersistentArrayMap
            PersistentHashMap]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            DocumentCreateEntity
            BaseDocument]))

(def ^:const entity-package-name "com.arangodb.entity")

;; *default-doc-class* is used to indicate
;; the class of the objects that we want RETURNED
(def ^:dynamic *default-doc-class* VPackSlice)

(defmulti serialize-doc class)
(defmulti deserialize-doc class)

(defmethod serialize-doc :default [o]
  ;; we provide some basic handling.
  ;; probably enough for most people.
  ;; we only try to serialize clojure maps
  (if (map? o)
    (vpack/pack o)
    o))

(defmethod deserialize-doc :default [o]
  ;; we provide some basic handling.
  ;; probably enough for most people.
  (let [c (class o)]
    (cond (= c VPackSlice) (vpack/unpack o)
          (= c BaseDocument) (bean o)
          :else o)))

(defmulti from-entity class)

(defn is-entity? [obj]
  (when obj
    (= entity-package-name (-> obj class .getPackage .getName))))

(defmethod from-entity :default [obj]
  ;; only some of the values will be entities
  (cond (is-entity? obj)
        ;; construct the entity recursively
        (try (.getDeclaringClass obj)
             ;; some entites only make sense as string
             ;; very much a heuristic here
             (str obj)
             (catch java.lang.IllegalArgumentException _
               (persistent!
                (reduce (fn [m [k v]]
                          (assoc! m k (from-entity v)))
                        (transient {}) (bean obj)))))
        ;; an array 'inside' an entity
        ;; we only map if we know the first item is an entity
        ;; this is because a MultiDocumententity may contain
        ;; return values or Entities depending on the call.
        (= java.util.ArrayList (-> obj class))
        (cond (empty? obj) []
              (is-entity? (.get obj 0)) (vec (map from-entity obj))
              :else (vec obj))
        :else obj))
