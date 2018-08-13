(ns clj-arangodb.arangodb.cursor
  (:require [clj-arangodb.arangodb.adapter :as ad])
  (:import [com.arangodb
            Predicate
            Function
            Consumer
            ArangoCursor]
           [com.arangodb.entity
            CursorEntity$Stats])
  (:refer-clojure :exclude [next first map filter count]))

(defn- as-pred ^com.arangodb.Predicate [pred]
  (reify com.arangodb.Predicate
    (test [this x] (pred x))))

(defn- as-fn ^com.arangodb.Function [f]
  (reify com.arangodb.Function
    (apply [this x] (f x))))

(defn- as-consumer ^java.util.function.Consumer [f]
  (reify java.util.function.Consumer
    (accept [this x] (f x))))

(defn has-next ^Boolean
  [^ArangoCursor cursor]
  (.hasNext cursor))

(defn get-type [^ArangoCursor cursor]
  (.getType cursor))

(defn next [^ArangoCursor cursor]
  (.next cursor))

(defn first [^ArangoCursor cursor]
  (.first cursor))

(defn foreach [^ArangoCursor cursor consume-fn]
  (.forEach cursor (as-consumer consume-fn)))

(defn map ^Iterable [cursor f]
  (.map cursor (as-fn f)))

(defn filter [cursor pred]
  (.filter cursor (as-pred pred)))

(defn any-match [cursor pred]
  (.anyMatch cursor (as-pred pred)))

(defn all-match [cursor pred]
  (.allMatch cursor (as-pred pred)))

(defn none-match [cursor pred]
  (.noneMatch cursor (as-pred pred)))

(defn collect-into [^ArangoCursor cursor target]
  (.collectInto cursor target))

(defn iterator [^ArangoCursor cursor]
  (.iterator cursor))

(defn as-list-remaining [^ArangoCursor cursor]
  (.asListRemaining cursor))

(defn get-count [^ArangoCursor cursor]
  (.getCount cursor))

(defn count ^Long
  [^ArangoCursor cursor]
  (.count cursor))

(defn get-stats ^CursorEntity$Stats
  [^ArangoCursor cursor]
  (ad/from-entity ^CursorEntity$Stats (.getStats cursor)))

(defn get-warnings [^ArangoCursor cursor]
  (ad/from-entity (.getWarnings cursor)))

(defn is-cached ^Boolean
  [^ArangoCursor cursor]
  (.isCached cursor))
