(ns clj-arangodb.velocypack.utils)

(defn normalize [k]
  (if (keyword? k) (name k) (str k)))
