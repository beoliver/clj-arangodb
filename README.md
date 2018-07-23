# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.
Much like monger, the java implementation is still visible.

functions are lispy versions of their java counterparts.
options are passed as map - with keywords written in `:cammelCase` - for example `{:someOption "a string" :anotherOption ["192.168.1.1", 8888]}`
For more information about what constitutes a valid option for a method you must consult the java api documentation.
As an aside, I considered adding destructuring to give the user nice feedback - but there are just to many of them! If you are interested -
have a look at the options namespace and the functions `fn-builder` and `build` - we just take the map and create method calls from the keys.

This wrapper exposes most of the available methods -
If we look at how the java code is used, in this example a new connecton is being made to a server.
```java
ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).host("192.168.182.50", 8888).build();
```
In the clojure version we are doing exactly the same thing under the hood.
```clojure
(import 'com.arangodb.Protocol)
(def arango-db (connect {:useProtocol Protocol/VST :host ["192.168.182.50" 8888]}))
```
## One very important namespace
the namespace `clj-arangodb.arangodb.adapter` contains 3 multimethods
```clojure
(defmulti serialize-doc class)
(defmulti deserialize-doc class)
(defmulti from-entity class)
```
On top of that the is 1 dynamic var `*default-doc-class*` that is bound to the class `com.arangodb.velocypack.VPackSlice`

To understand the design of this library it is important to undererstand a little bit about how the java client works.

When you want to send data to ArangoDB the data can be in `json` `POJO` (plain old Java object) or a format called a `VPackSlice`
VPackSlices are optimised for fast transmition (by default the java driver uses something called velocy-streams which as you might
have guessed have a lot to do with VPackSlices or *velocy packs*.

As clojurists we like to work with maps and it gets really annoying having to wrap all of your calls in some "to-string" -
However, as there are multiple options in how data can be sent, I have tried to be as un-opinionated as possible.
I also didn't want to have any dependencies (so no external json libs - pjson is fast but its up to you).

lets see what happens
```clojure
(ns user
  (:require [clj-arangodb.arangodb.core :as ar]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]))

(def conn (ar/connect {:user "test"}))
(def db (ar/create-and-get-database conn "myDB"))
(def coll (d/create-and-get-collection db "myColl"))
```

By default calls that return a `Entity` of some kind are wrapped with `adapter/from-entity`.
`Entity` results are only data - ie they are not handles.
The default for this is to call `bean` and then examines the values under the keys - this approach has been trail and error, but the
default aim to give the user readable and usable results - in general if the entity contains results, these results are *not*
desearialzed. all sub classes are - (some are converted to string to give sensible data)

As far as I can tell there is no Abstract Entity class to dispatch on... which is a bit awkward

```clojure
(defmethod from-entity :default [obj]
  (cond (is-entity? obj)
        (try (.getDeclaringClass obj)
             ;; some entites only make sense as string
             ;; very much a heuristic here
             (str obj)
             (catch java.lang.IllegalArgumentException _
               (persistent!
                (reduce (fn [m [k v]]
                          (assoc! m k (from-entity v)))
                        (transient {}) (bean obj)))))
        ;; an array 'inside' an entity
        ;; we only map if we know the first item is an entity
        ;; this is because a MultiDocumententity may contain
        ;; return values or Entities depending on the call.
        (= java.util.ArrayList (-> obj class))
        (cond (empty? obj) []
              (is-entity? (.get obj 0)) (vec (map from-entity obj))
              :else (vec obj))
        :else obj))
```

Lets add a document
```clojure
user> (c/insert-document coll {:name "nested" :data {:a {:b [1 2 3] :c true}}})
{:class com.arangodb.entity.DocumentCreateEntity, :id "helloColl/360443", :key "360443", :new nil, :old nil, :rev "_XKHy-X---_"}
```
Now, it's time to get the data back again.
```clojure
user> (c/get-document coll "360443")
{:_id "helloColl/360443", :_key "360443", :_rev "_XKHy-X---_", :data {:a {:b [1 2 3], :c true}}, :name "nested"}
```
By default maps are packed and unpacked as `VPackSlice` objects - the implementation of this is the velocypack namespace.

if we pass a class as well we can get a different type back
```clojure
user> (c/get-document c "360443" String)
"{\"_id\":\"helloColl\\/360443\",\"_key\":\"360443\",\"_rev\":\"_XKHy-X---_\",\"data\":{\"a\":{\"b\":[1,2,3],\"c\":true}},\"name\":\"nested\"}"
user> (c/get-document c "360443" java.util.Map)
{"data" {"a" {"b" [1 2 3], "c" true}}, "_rev" "_XKHy-X---_", "name" "nested", "_id" "helloColl/360443", "_key" "360443"}
user> (c/get-document c "360443" BaseDocument)
{:class com.arangodb.entity.BaseDocument, :id "helloColl/360443", :key "360443", :properties {"data" {"a" {"b" [1 2 3], "c" true}}, "name" "nested"}, :revision "_XKHy-X---_"}
```
If you want to use a json serializer/deserializer then just extend the multimethods `serialize-doc` and `deserialize-doc` for the class `String`

Have a play - and remeber the multimethods! - if you don't like the data you are getting, change it...
