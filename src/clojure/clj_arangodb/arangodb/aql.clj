(ns clj-arangodb.arangodb.aql
  (:require [clojure.string :as str]))

(defmulti serialize class)
(defmulti aql-lang (fn [x & _] x))

;;; janky handling for option maps for graphs

(defn- parse-traversal-options
  [{bfs :bfs {v :vertices e :edges} :unique :as options}]
  (if (empty? options)
    ""
    (format "OPTIONS {%s%s%s}"
            (if-not bfs
              ""
              "bfs : true, ")
            (if-not v
              ""
              (str "uniqueVertices : \"" (name v) "\", "))
            (if-not e
              ""
              (str "uniqueEdges : \"" (name e) "\"")))))

(defn- parse-graph-or-collections [graph collections]
  (if graph
    (str "GRAPH " (serialize graph))
    (str/join "," (for [c collections]
                    (if-not (sequential? c)
                      (serialize c)
                      (str (str/upper-case (name (first c))) " "
                           (serialize (second c))))))))

(defn- parse-shortest-path
  [{:keys [type start shortest-path graph collections options]
    :as traversal}]
  (let [type (str/upper-case (name type))]
    (format "%s SHORTEST_PATH %s TO %s %s %s"
            type
            (serialize start)
            (serialize shortest-path)
            (parse-graph-or-collections graph collections)
            (parse-traversal-options options))))

(defn- format-aql-traversal
  [o] (if-not (map? o)
        (serialize o)
        (let [{:keys [depth type start shortest-path graph collections options]} o]
          (if shortest-path
            (parse-shortest-path o)
            (let [mn (or (first depth) 1)
                  mx (or (second depth) mn)]
              (format "%s %s %s %s %s"
                      (str mn ".." mx)
                      (str/upper-case (name type))
                      (serialize start)
                      (parse-graph-or-collections graph collections)
                      (parse-traversal-options options)))))))

(defmethod serialize nil [o] "null")
(defmethod serialize :default [o] o)

(defmethod serialize clojure.lang.PersistentVector
  ;; if a vetcor has a keyword in position 0 then it is
  ;; treated as a language construct
  [o]
  (if (keyword? (first o))
    (apply aql-lang o)
    (format "[%s]" (str/join "," (map serialize o)))))

(defmethod serialize clojure.lang.APersistentMap
  [o]
  (let [serialize-entry (fn [[k v]]
                          (format "%s:%s" (name k) (serialize v)))]
    (format "{%s}" (str/join "," (map serialize-entry o)))))

(defmethod serialize clojure.lang.APersistentSet
  [o]
  (format "[%s]" (str/join "," (map serialize o))))

(defmethod aql-lang :WITH
  ([op collections & body]
   (let [lables (str/join "," collections)
         parsed-body (str/join "\n" (map serialize body))]
     (format "(WITH %s\n%s)" lables parsed-body))))

(defmethod aql-lang :FOR
  [_ bindings & body]
  (let [params (butlast bindings)
        expr (last bindings)]
    (format "FOR %s IN %s\n%s"
            (str/join "," (map serialize params))
            (format-aql-traversal expr)
            (str/join "\n" (map serialize body)))))

(defmethod aql-lang :LET
  [op bindings & body]
  (let [var-expr-pairs (partition 2 bindings)
        parsed-bindings (str/join "\n"
                                  (for [[v e] var-expr-pairs]
                                    (format "LET %s = %s" v (serialize e))))
        parsed-body (str/join "\n" (map serialize body))]
    (str parsed-bindings "\n" parsed-body)))

(defmethod aql-lang :COLLECT
  ([op bindings & [{into-expr :into keep-vars :keep options :options :as extra}]]
   (let [parsed-bindings (str/join ","
                                   (for [[v e] (partition 2 bindings)]
                                     (format "%s = %s" v (serialize e))))
         parsed-into (when into-expr
                       (if-not (vector? into-expr)
                         (name into-expr)
                         (format "%s = %s"
                                 (name (first into-expr))
                                 (serialize (second into-expr)))))
         parsed-keep-vars (when keep-vars
                            (if-not (vector? keep-vars)
                              (name keep-vars)
                              (str/join "," (map name keep-vars))))
         parsed-options (when options
                          (serialize options))]
     (cond-> (format "COLLECT %s" parsed-bindings)
       parsed-into (#(format "%s INTO %s" % parsed-into))
       parsed-keep-vars (#(format "%s KEEP %s" % parsed-keep-vars))
       parsed-options (#(format "%s OPTIONS %s" % parsed-options))))))

(defmethod aql-lang :RETURN
  [op expression]
  (format "RETURN %s" (serialize expression)))

(defmethod aql-lang :RETURN-DISTINCT
  [op expression]
  (format "RETURN DISTINCT %s" (serialize expression)))

(defmethod aql-lang :UPDATE
  [_ document-key object collection]
  (format "UPDATE %s WITH %s IN %s"
          (serialize document-key)
          (serialize object)
          (serialize collection)))

(defmethod aql-lang :REPLACE
  [_ document-key object collection]
  (format "REPLACE %s WITH %s IN %s"
          (serialize document-key)
          (serialize object)
          (serialize collection)))

(defmethod aql-lang :REMOVE
  [_ document-key collection]
  (format "REMOVE %s IN %s"
          (serialize document-key)
          (serialize collection)))

(defmethod aql-lang :INSERT
  [op document coll]
  (format "INSERT %s INTO %s"
          (serialize document)
          (serialize coll)))

(defmethod aql-lang :FILTER
  [op expr] (format "FILTER %s" (serialize expr)))

(defmethod aql-lang :SORT
  [op & exprs]
  (format "SORT %s" (str/join
                     ","
                     (map (fn [x]
                            (if-not (vector? x)
                              (str x)
                              (let [k (first x)
                                    direction (second x)]
                                (str x (str/upper-case (name direction))))))
                          exprs))))

(defmethod aql-lang :LIMIT
  ([op cnt] (format "LIMIT %s" cnt))
  ([op offset cnt] (format "LIMIT %s, %s" offset cnt)))

;;; infix

(defmethod aql-lang :AND
  [op expr-1 expr-2]
  (format "(%s AND %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :OR
  [op expr-1 expr-2]
  (format "(%s OR %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :EQ
  [op expr-1 expr-2]
  (format "(%s == %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :LT
  [op expr-1 expr-2]
  (format "(%s < %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :GT
  [op expr-1 expr-2]
  (format "(%s > %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :LTEQ
  [op expr-1 expr-2]
  (format "(%s <= %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :GTEQ
  [op expr-1 expr-2]
  (format "(%s >= %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :NE
  [op expr-1 expr-2]
  (format "(%s != %s)" (serialize expr-1) (serialize expr-2)))

(defmethod aql-lang :default
  [op & args]
  (format "%s(%s)" (name op) (str/join "," (map serialize args))))
