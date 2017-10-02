(ns clj-arangodb.arangodb.by-name
  (:require
   [clj-arangodb.arangodb.core :as a]
   [clj-arangodb.arangodb.databases :as d]
   [clj-arangodb.arangodb.collections :as c])
  )

;;; An alternative interface that allows calling methods using the
;;; string representation of databases and collections.

;;; this namespace can replace databases and collections

;;; all functions take a connection as their first parameter
;;; why not multimethods that allow for dispatch like

;; (defn create-collection
;;   ([conn db-name coll-name])
;;   ([db coll-name]))

;;; because options can be passed as well

;; (defn create-collection
;;   ([conn db-name coll-name] ...)
;;   ([db coll-name] ...)
;;   ([conn db-name coll-name options] ...)
;;   ([db coll-name options] ...))

;;; now we have [db coll-name options] and [conn db-name coll-name] with three args

(defn create-collection
  ([conn db-name coll-name options]
   (-> (a/get-db db-name) (d/create-collection coll-name options)))
  ([conn db-name coll-name]
   (create-collection conn db-name coll-name nil)))

(defn get-collection
  ([conn db-name coll-name]
   (-> (a/get-db db-name) (d/get-collection coll-name))))

(defn get-collections
  ([conn db-name]
   (-> (a/get-db db-name) d/get-collections)))
