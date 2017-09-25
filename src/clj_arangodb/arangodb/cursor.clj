(ns clj-arangodb.arangodb.cursor)

(defn as-list [cursor]
  (.asListRemaining cursor))
