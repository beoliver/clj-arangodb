(ns clj-arangodb.arangodb.adapter
  (:require [clj-arangodb.velocypack.core :as vpack])
  (:import [clojure.lang
            PersistentArrayMap
            PersistentHashMap]
           [com.arangodb.velocypack
            VPackSlice]
           [com.arangodb.entity
            BaseDocument]))

(def ^:dynamic *default-doc-class* VPackSlice)

(defmulti serialize-doc class)
(defmulti deserialize-doc class)

(defmethod serialize-doc :default [o] o)
(defmethod deserialize-doc :default [o] o)

(defmulti from-entity class)
(defmulti from-collection class)
(defmulti from-graph class)
(defmulti from-cursor class)

(defmethod from-entity :default [o] (bean o)) ; only contain data
(defmethod from-collection :default [o] o) ; needed for calling methods
(defmethod from-graph :default [o] o) ; needed for calling methods
(defmethod from-cursor :default [o] o) ; needed for calling methods

;; (require '[clj-arangodb.arangodb.adapter :as adapter])
;; (require '[clj-arangodb.velocypack.core :as vpack])
;; (defmethod adapter/deserialize-doc VPackSlice [o]
;;   (vpack/unpack o keyword))

;;; up to the user how to encode clojure maps;

;; (defmethod serialize-doc PersistentHashMap
;;   [o] (vpack/pack o))
;; (defmethod serialize-doc PersistentArraymap
;;   [o] (vpack/pack o))

;; if you want to use a json library just extend it here

;; (defmethod serialize-doc PersistentHashMap
;;   [o] (your-json-lib/to-string o))
;; (defmethod serialize-doc PersistentArraymap
;;   [o] (your-json-lib/to-string o))

;;; up to the user how to deserialize String
;; (defmethod deserialize-doc String [o]
;;   (your-json-lib/from-string o keyword))
