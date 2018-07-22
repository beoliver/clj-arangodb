# clj-arangodb

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.
Much like monger, the java implementation is still visible.

functions are lispy versions of their java counterparts.
options are passed as map - with keywords written in `:cammelCase` - for example `{:someOption "a string" :anotherOption ["192.168.1.1", 8888]}`
For more information about what constitutes a valid option for a method you must consult the java api documentation.
As an aside, I considered adding destructuring to give the user nice feedback - but there are just to MANY of them!

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
There are no lispy-key/keywords for these things I'm afraid.

## One very important namespace
the namespace `clj-arangodb.arangodb.adapter` currently contains 6 multimethods (3 of which are important to understand)
```clojure
(defmulti serialize-doc class)
(defmulti deserialize-doc class)
(defmulti from-entity class)

(defmethod serialize-doc :default [o] o)
(defmethod deserialize-doc :default [o] o)
(defmethod from-entity :default [o] (bean o))
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

(def res (c/insert-document coll {:name "clj-arango" :version "0.0.1"}))
VPackValueTypeException Expecting type OBJECT  com.arangodb.velocypack.VPackSlice.objectIterator (VPackSlice.java:772)
```
Ok.. so it's broken? Not quite - remeber those multi methods? well, in any call that will send documents
to the database, the multimethod `serialize-doc` is called. Currently it defaults to whatever you give it
so in our case a `clojure.lang.PersistentArrayMap`.

Perhaps you find a better vpack library - but until you do, you can find one under `clj-arangodb.velocypack.core`
This ns provides two functions `pack` and `unpack`
So lets extend the multimethod
```clojure
(require '[clj-arangodb.arangodb.adapter :as adapter]
	 '[clj-arangodb.velocypack.core :as vpack])
(defmethod adapter/serialize-doc clojure.lang.PersistentArrayMap [o]
  (vpack/pack o))
```
If we now try again:
```clojure
user> (def res (c/insert-document coll {:name "clj-arango" :version "0.0.1"}))
#'user/res
user> res
{:class com.arangodb.entity.DocumentCreateEntity, :id "helloColl/298178", :key "298178", :new nil, :old nil, :rev "_XJ8g7Yi--_"}
```
Well look at that! By default calls that return a `Entity` of some kind are wrapped with `adapter/from-entity`. by default this
calls `bean` on the object - `Entity` results are only data - ie they are not handles.

Lets add another
```clojure
user> (c/insert-document coll {:name "nested" :data {:a {:b [1 2 3] :c true}}})
{:class com.arangodb.entity.DocumentCreateEntity, :id "helloColl/298604", :key "298604", :new nil, :old nil, :rev "_XJ8k7mu--_"}
```
Now, it's time to get the data back again.
```clojure
user> (c/get-document coll "298604")
#object[com.arangodb.velocypack.VPackSlice 0x20fa57af "{\"_id\":\"helloColl\\/298604\",\"_key\":\"298604\",\"_rev\":\"_XJ8k7mu--_\",\"data\":{\"a\":{\"b\":[1,2,3],\"c\":true}},\"name\":\"nested\"}"]
```
if we pass a class as well we can get a different type back
```clojure
user> (c/get-document coll "298604" String)
"{\"_id\":\"helloColl\\/298604\",\"_key\":\"298604\",\"_rev\":\"_XJ8k7mu--_\",\"data\":{\"a\":{\"b\":[1,2,3],\"c\":true}},\"name\":\"nested\"}"
```
again we will extend a multimethod
```clojure
(defmethod adapter/deserialize-doc VPackSlice [o]
  (vpack/unpack o))

user> (c/get-document coll "298604")
{"_id" "helloColl/298604", "_key" "298604", "_rev" "_XJ8k7mu--_", "data" {"a" {"b" [1 2 3], "c" true}}, "name" "nested"}
```

But... as we like keywords, and as we can have keywords, we might as well use them!
```clojure
(defmethod adapter/deserialize-doc VPackSlice [o]
  (v/unpack o keyword))

user> (c/get-document coll "298604")
{:_id "helloColl/298604", :_key "298604", :_rev "_XJ8k7mu--_", :data {:a {:b [1 2 3], :c true}}, :name "nested"}
```

Have a play - and remeber the multimethods!