# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.
Much like monger, the java implementation is still visible.

Functions are lispy versions of their java counterparts.
Options are passed as maps - with keywords written in `:cammelCase` - for example `{:someOption "a string" :anotherOption ["192.168.1.1", 8888]}`
For more information about what constitutes a valid option for a method you must consult the java api documentation.

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
On top of that there is 1 dynamic var `*default-doc-class*` that is bound to the class `com.arangodb.velocypack.VPackSlice`

To understand the design of this library it is important to undererstand a little bit about how the java client works.

When you want to send data to ArangoDB the data can be in `json` `POJO` (plain old Java object) or a format called a `VPackSlice`
VPackSlices are optimised for fast transmition (by default the java driver uses something called velocy-streams which as you might
have guessed have a lot to do with VPackSlices or *velocy packs*.

As clojurists we like to work with maps and it gets really annoying having to wrap all of your calls in some "to-string" -
However, as there are multiple options in how data can be sent, I have tried to be as un-opinionated as possible.
I also didn't want to have any dependencies (so no external json libs - pjson is fast but its up to you).

While the ArangoDB Java driver allows for custom serializer/deserialiser modules - as of now there is not one for Clojure datastructures - it is on my list of things to do!

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
The default for this is to call `bean` and then examines the values under the keys.
In general if the entity contains results, these results are *not*
desearialzed. all sub classes are - (some are converted to string to give sensible data)

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

## AQL

The AQL query syntax can be represented as clojure data structures, there is no EBFN document at the moment so you will have to read the source file, an example taken from one of the tests:
In this example the FOR statement is used to execute a graph query
```clojure
(deftest brans-parents-test
  (h/with-db [db td/game-of-thrones-db-label]
    (let [parents #{"Ned" "Catelyn"}
          query [:FOR ["c" "Characters"]
                 [:FILTER [:EQ "c.name" "\"Bran\""]]
                 [:FOR ["v" {:start "c"
                             :type :outbound
                             :depth [1 1]
                             :collections ["ChildOf"]}]
                  [:RETURN "v.name"]]]]
      (is (= (set (d/query db query String))
             (set (map adapter/deserialize-doc (d/query db query)))
             parents)))))
```


Have a play - and remeber the multimethods! - if you don't like the data you are getting, change it...
