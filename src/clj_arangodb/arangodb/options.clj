(ns clj-arangodb.arangodb.options
  (:import com.arangodb.model.CollectionCreateOptions
           com.arangodb.entity.CollectionType
           com.arangodb.model.DocumentCreateOptions
           com.arangodb.model.DocumentReadOptions
           com.arangodb.model.DocumentReplaceOptions
           com.arangodb.model.DocumentUpdateOptions
           com.arangodb.model.DocumentDeleteOptions
           com.arangodb.model.DocumentExistsOptions
           com.arangodb.model.DocumentReadOptions
           com.arangodb.model.GraphCreateOptions
           com.arangodb.model.GeoIndexOptions
           com.arangodb.model.VertexCollectionCreateOptions
           com.arangodb.model.VertexCreateOptions
           com.arangodb.model.VertexDeleteOptions
           com.arangodb.model.VertexReplaceOptions
           com.arangodb.model.VertexUpdateOptions
           com.arangodb.model.EdgeCreateOptions
           com.arangodb.model.EdgeDeleteOptions
           com.arangodb.model.EdgeReplaceOptions
           com.arangodb.model.EdgeUpdateOptions
           com.arangodb.model.AqlQueryOptions
           com.arangodb.ArangoDB$Builder))

(defn fn-builder
  "given a map entry create a function that wraps a method call
  using the method with the same name as the keyword.
  Arguments single multiple arguments should be in a seq"
  [[method-key args]]
  (let [method-name (symbol (name method-key))
        args (flatten (list args))]
    (eval `(fn [x#] (. x# ~method-name ~@args)))))

(defn build
  "given a class name and map of options create a sequence of function calls
  and call them sequentially using a new `object` of class `class`"
  [class options]
  (if (map? options)
    (reduce (fn [acc f]
              (f acc)) (eval `(new ~class)) (map fn-builder options))
    options))
