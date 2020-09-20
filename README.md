# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)
[![CircleCI](https://circleci.com/gh/beoliver/clj-arangodb.svg?style=shield)](https://circleci.com/gh/beoliver/clj-arangodb)

### A Clojure interface for the `arangodb-java-driver`

The maintainers of arangodb provide a [java driver](https://www.arangodb.com/docs/stable/drivers/java-getting-started.html) for communicating with an arangodb server. `clj-arangodb` provides a _thin_ (and incomplete) abstraction.

## Getting started

For the most up to date information it is best to consult the official java [documentation](https://www.arangodb.com/docs/stable/drivers/java-reference.html). In general functions are lispy versions of their java counterparts (methods).

Options are passed as maps. Keys can be _keywords_ or _strings_, but should be written using `cammelCase`. If an option takes multiple arguments then a vector should be used.

For more information about what constitutes a valid option for a method you must consult the java api documentation.

### Creating a connection

Assuming that you have created a `user` in the arango database...

```clojure
(ns user
  (:require [clj-arangodb.arangodb.core :as a]
            [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.collections :as c]
            [clj-arangodb.arangodb.adapter :as adapter])
  (:import [com.arangodb Protocol]
           [com.arangodb.entity BaseDocument]
           [com.arangodb.velocypack VPackSlice]))

(defonce conn ;; connections are thread safe
  (let [config {:useProtocol Protocol/VST
                :user "test"
                :host ["127.0.0.1" 8529]}]
    (a/connect config)))
```

### Databases

```clojure
user> (a/database? conn "testDB")
false
user> (def db (a/create-and-get-database conn "testDB"))
#'user/db
user> db
#object[com.arangodb.internal.ArangoDatabaseImpl 0x38f9457a "com.arangodb.internal.ArangoDatabaseImpl@38f9457a"]
user> (a/database? conn "testDB")
true
user> (a/get-databases conn)
["_system" "testDB"]
```

### Collections

```clojure
user> (d/collection? db "testColl")
false
user> (def coll (d/create-and-get-collection db "testColl"))
#'user/coll
user> (d/collection? db "testColl")
true
user> (def coll-again (d/get-collection db "testColl"))
#'user/coll-again
user> (= coll coll-again)
false
user> coll
#object[com.arangodb.internal.ArangoCollectionImpl 0x23266a4f "com.arangodb.internal.ArangoCollectionImpl@23266a4f"]
user> coll-again
#object[com.arangodb.internal.ArangoCollectionImpl 0x33859f41 "com.arangodb.internal.ArangoCollectionImpl@33859f41"]
user> (= (adapter/from-entity (:info (bean coll)))
         (adapter/from-entity (:info (bean coll-again))))
true
```

By default functions that return a `Entity` of some kind are wrapped with `adapter/from-entity`.
`Entity` results are only data - ie they are not handles.
The default for this is to call `bean` and then examines the values under the keys.
In general if the entity contains results, these results are _not_
desearialzed. all sub classes are - (some are converted to string to give sensible data)

### Documents

inserting a document

```clojure
user> (c/insert-document coll {:hello "world"})
{:class com.arangodb.entity.DocumentCreateEntity,
 :id "testColl/34730",
 :key "34730",
 :new nil,
 :old nil,
 :rev "_bH1Uxmq---"}
```

#### Retrieving a document.

By default when retreiving a document the `VPackSlice` class is used. This is then parsed using the [cheshire(https://github.com/dakrone/cheshire) json library.

```clojure
user> (c/get-document coll "34730")
{:_key "34730",
 :_id "testColl/34730",
 :_rev "_bH1Uxmq---",
 :hello "world"}

user> (c/get-document coll "34730" VPackSlice)
{:_key "34730",
 :_id "testColl/34730",
 :_rev "_bH1Uxmq---",
 :hello "world"}
```

By passing a class as well we can get a different type back.
The `BaseDocument` (belonging to ArangoDB) class is converted by calling `bean`.

```clojure
user> (c/get-document coll "34730" BaseDocument)
{:class com.arangodb.entity.BaseDocument,
 :id "testColl/34730",
 :key "34730",
 :properties {"hello" "world"},
 :revision "_bH1Uxmq---"}
```

```clojure
user> (c/get-document coll "34730" String)
"{\"_key\":\"34730\",\"_id\":\"testColl\\/34730\",\"_rev\":\"_bH1Uxmq---\",\"hello\":\"world\"}"
```

```clojure
user> (c/get-document coll "34730" java.util.Map)
{"_key" "34730",
 "_id" "testColl/34730",
 "_rev" "_bH1Uxmq---",
 "hello" "world"}
```

If you want to use a custom json serializer/deserializer then you can extend the multimethods `serialize-doc` and `deserialize-doc` for the `String` class.

## AQL

The AQL query syntax can be represented as clojure data structures, there is no EBFN document at the moment so you will have to read the source file, an example taken from one of the tests:
In this example the FOR statement is used to execute a graph query

```clojure
(ns user
  (:require [clj-arangodb.arangodb.databases :as d]
            [clj-arangodb.arangodb.test-data :as td]
            [clj-arangodb.arangodb.helper :as h]))
```

```clojure
user> (td/init-game-of-thrones-db)
nil
user> (h/with-db [db td/game-of-thrones-db-label]
        (let [query [:FOR ["c" "Characters"]
                     [:FILTER [:EQ "c.name" "\"Bran\""]]
                     [:FOR ["v" {:start "c"
                                 :type :outbound
                                 :depth [1 1]
                                 :collections ["ChildOf"]}]
                      [:RETURN "v.name"]]]]
          (println (vec (d/query db query String)))))
[Ned Catelyn]
```

Have a play - and remeber the multimethods! - if you don't like the data you are getting, change it...
