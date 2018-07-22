(ns clj-arangodb.arangodb.utils
  (:require [clojure.reflect :as r])
  (:import com.arangodb.Protocol))

;; magic function creation!

(defn lisp-ify [cammelCase]
  (-> cammelCase
      str
      (clojure.string/replace #"(.)([A-Z][a-z]+)" "$1-$2")
      (clojure.string/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (clojure.string/lower-case)
      symbol))

(defn class-as-param-name [class-name]
  (-> class-name
      str
      (clojure.string/split #"\.")
      last
      lisp-ify))

(defn wrap-method-member [{:keys [name
                                  return-type
                                  declaring-class
                                  parameter-types
                                  exception-types
                                  flags] :as member}]
  (let [dot-name (symbol (str "." name))
        o# (class-as-param-name declaring-class)
        arg-types-and-names (into [o#] (map class-as-param-name parameter-types))]
    `(~(into [o#] (map class-as-param-name parameter-types))
      ~(cons (vary-meta dot-name assoc :tag declaring-class)
             arg-types-and-names))))

(defn generate-multi-arity-decls [[name implementations]]
  `(defn ~(lisp-ify name)
     ~@(map wrap-method-member implementations)))

(defn wrap-object-publics [o]
  (as-> (r/reflect o) $
    (:members $)
    (filter (fn [x] (some #{:public} (:flags x))) $)
    (group-by :name $)
    (map generate-multi-arity-decls $)))

(defn load-object-publics [o]
  (doseq [o (wrap-object-publics o)]
    (eval o)))
