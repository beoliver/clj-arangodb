(ns clj-arangodb.velocypack.utils)

(defn key->str [k]
  (if (keyword? k) (name k) (str k)))

(defn str->key [k]
  (if (Character/isDigit (.charAt k 0))
    (try (Integer. k)
         (catch Exception _
           (try (Float. k)
                (catch Exception _
                  (keyword k)))))
    (keyword k)))
