(ns clj-arangodb.arangodb.cursor
  (:import com.arangodb.internal.ArangoCursorImpl
           com.arangodb.internal.ArangoCursorIterator
           com.arangodb.internal.ArangoCursorExecute
           com.arangodb.ArangoCursor)
  (:refer-clojure :exclude [next first map filter count]))

(defn has-next [cursor]
  (.hasNext cursor))

(defn get-type [^ArangoCursor cursor]
  (.getType cursor))

(defn next [^ArangoCursor cursor]
  (.next cursor))

(defn first [^ArangoCursorImpl cursor]
  (.first cursor))

(defn foreach [^Iterable cursor action]
  (.forEach cursor action))

(defn map [cursor mapper]
  (.map cursor mapper))

(defn filter [cursor pred]
  (.filter cursor pred))

(defn any-match [cursor pred]
  (.anyMatch cursor pred))

(defn all-match [cursor pred]
  (.allMatch cursor pred))

(defn none-match [cursor pred]
  (.noneMatch cursor pred))

(defn collect-into [cursor target]
  (.collectInto cursor target))

(defn iterator [^ArangoCursor cursor]
  (.iterator cursor))

(defn as-list-remaining [^ArangoCursor cursor]
  (.asListRemaining cursor))

(defn get-count [^ArangoCursor cursor]
  (.getCount cursor))

(defn count [^ArangoCursor cursor]
  (.count cursor))

(defn get-stats [^ArangoCursor cursor]
  (.getStats cursor))

(defn get-warnings [^ArangoCursor cursor]
  (.getWarnings cursor))

(defn is-cached [^ArangoCursor cursor]
  (.isCached cursor))
