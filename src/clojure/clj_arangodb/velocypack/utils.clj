(ns clj-arangodb.velocypack.utils)

(defn key->str ^String [k]
  {:inline (fn [k] `(if (keyword? ~k) (name ~k) (str ~k)))}
  (if (keyword? k) (name k) (str k)))

(defn str->key [^String k]
  (if-not (Character/isDigit (.charAt k 0))
    (keyword k)
    (try (Integer. k)
         (catch Exception _
           (try (Float. k)
                (catch Exception _
                  (keyword k)))))))
