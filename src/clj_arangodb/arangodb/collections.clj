(ns clj-arangodb.arangodb.collections
  (:require [clojure.set :as set]
            [pjson.core :as json]
            [clojure.walk :as walk]
            [clj-arangodb.arangodb.utils :as utils]
            [clj-arangodb.arangodb.graph :as g]
            [clojure.reflect :as r])
  (:import
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoCollection
   com.arangodb.entity.CollectionEntity
   com.arangodb.ArangoDBException
   com.arangodb.velocypack.VPackSlice))






(defn insert-doc
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:_id` `:_key` `:_new` and `:_rev`"
  [^ArangoCollection coll doc]
  (-> (.insertDocument coll doc)
      bean
      (dissoc :class)
      (set/rename-keys {:id :_id :key :_key :new :_new :rev :_rev})))


(defn MultiDocumentEntity->map [o]
  (-> o
      bean
      (update :documentsAndErrors
              #(map (fn [x] (-> (bean x)
                                (dissoc :class)
                                (set/rename-keys {:id :_id
                                                  :key :_key
                                                  :new :_new
                                                  :rev :_rev}))) %))
      (get :documentsAndErrors)))

(defn insert-docs
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:id` `:key` `:new` and `:rev`"
  ([^ArangoCollection coll ^java.util.ArrayList docs]
   (-> (.insertDocuments coll docs)
       MultiDocumentEntity->map))
  ([^ArangoDatabase db ^String coll-name ^java.util.ArrayList docs]
   (-> db (.collection coll-name) (insert-docs docs)))
  ([^ArangoDB conn ^String db-name ^String coll-name ^java.util.ArrayList docs]
   (-> conn (.db db-name) (insert-docs coll-name docs))))

(defn update-doc
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String)."
  ([coll key doc]
   (.updateDocument coll key doc))
  ([db coll-name key doc]
   (-> db (.collection coll-name) (.updateDocument key doc)))
  ([conn db-name coll-name key doc]
   (-> conn (.db db-name) (.collection coll-name) (.updateDocument key doc))))

(defn update-docs
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:id` `:key` `:new` and `:rev`"
  ([coll docs]
   (-> (.updateDocuments coll (java.util.ArrayList. docs))
       MultiDocumentEntity->map))
  ([db coll-name docs]
   (-> db (.collection coll-name) (update-docs docs)))
  ([conn db-name coll-name docs]
   (-> conn (.db db-name) (update-docs coll-name docs))))

(defn replace-doc
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:id` `:key` `:new` and `:rev`"
  ([coll doc]
   (-> (.replaceDocument coll doc) bean (dissoc :class)))
  ([db coll-name doc]
   (-> db (.collection coll-name) (replace-doc doc)))
  ([conn db-name coll-name doc]
   (-> conn (.db db-name) (replace-doc coll-name doc))))

(defn replace-docs
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:id` `:key` `:new` and `:rev`"
  ([coll docs]
   (-> (.replaceDocuments coll (java.util.ArrayList. docs))
       MultiDocumentEntity->map))
  ([db coll-name docs]
   (-> db (.collection coll-name) (replace-docs docs)))
  ([conn db-name coll-name docs]
   (-> conn (.db db-name) (replace-docs coll-name docs))))

(defn delete-doc
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:id` `:key` `:new` and `:rev`"
  ([coll doc]
   (-> (.deleteDocument coll doc) bean (dissoc :class)))
  ([db coll-name doc]
   (-> db (.collection coll-name) (delete-doc doc)))
  ([conn db-name coll-name doc]
   (-> conn (.db db-name) (delete-doc coll-name doc))))

(defn delete-docs
  "works with POJOs (e.g. MyObject), VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:id` `:key` `:new` and `:rev`"
  ([coll docs]
   (-> (.deleteDocuments coll (java.util.ArrayList. docs))
       MultiDocumentEntity->map))
  ([db coll-name docs]
   (-> db (.collection coll-name) (delete-docs docs)))
  ([conn db-name coll-name docs]
   (-> conn (.db db-name) (delete-docs coll-name docs))))

(defn get-vpack-doc-by-key
  ([coll k] (.getDocument coll k VPackSlice))
  ([db coll-name k] (-> db (.collection coll-name) (.getDocument k VPackSlice)))
  ([conn db-name coll-name k] (-> conn
                                  (.db db-name)
                                  (.collection coll-name)
                                  (.getDocument k VPackSlice))))

(defn get-json-doc-by-key*
  [{:keys [conn db coll]} k]
  (-> conn (.db db) (.collection coll) (.getDocument k String)))

(defn get-json-doc-by-key
  ([coll k]
   (.getDocument coll k String))
  ([db coll-name k]
   (-> db (.collection coll-name) (.getDocument k String)))
  ([conn db-name coll-name k]
   (-> conn (.db db-name) (.collection coll-name) (.getDocument k String))))

(defn get-doc-by-key
  ([coll k] (-> (get-json-doc-by-key coll k)
                json/read-str
                walk/keywordize-keys))
  ([db coll-name k] (-> (get-json-doc-by-key db coll-name k)
                        json/read-str
                        walk/keywordize-keys))
  ([conn db-name coll-name k] (-> (get-json-doc-by-key conn db-name coll-name k)
                                  json/read-str
                                  walk/keywordize-keys)))

(def get-edge-by-key get-doc-by-key)

(defn get-bean-doc-by-key
  ([coll k class] (.getDocument coll k class))
  ([db coll-name k class] (-> db (.collection coll-name) (.getDocument k class)))
  ([conn db-name coll-name k class] (-> conn
                                        (.db db-name)
                                        (.collection coll-name)
                                        (.getDocument k class))))


(defn get-status [coll]
  (str (.getStatus coll)))

(defn get-name [coll]
  (.getName coll))

(defn get-type [coll]
  (str (.getType coll)))

(defn get-id [coll]
  (.getId coll))
