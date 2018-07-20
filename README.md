# clj-arangodb

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a clean interface. Much like monger, the java implementation is still visible.

If we look at how the java code is used, in this example a new connecton is being made to a server.
```java
ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).host("192.168.182.50", 8888).build();
```
In the clojure version we are doing exactly the same thing under the hood.
```clojure
(def arango-db (connect {:useProtocol :vst :host "192.168.182.50" :port 8888}))
```
Where possible the keys to maps are identical to the methods in the java-driver. By default the java driver connectes to arangodb using something called a velocystream.
this has some implications - firstly it is in theory more efficient, secondly we need to be aware of something called velocypacks - but we will come to that later.

So lets begin with a simple example.
```clojure
(require '[clj-arangodb.arangodb.core :as arango])
;; the .core ns provides functions for creating connections and working with databases
(def conn (arango/connect {:user "dev" :password "123"}))
;; we create a connection - this is for my local instace. If no credentials are used then
;; it falls back to the defaults for the java-driver ("root")
;; so I guess the first thing that we want to do is create a database.
(arango/create-database conn "userDB")
(def db (arango/db conn "userDB"))
;; the naming of functions mirrors those of the java client which makes finding information
;; about them straght forward.
;; db is a database "handle". In real terms, it is a java object with certain methods on it.
(require '[clj-arangodb.arangodb.databases :as d])
(d/create-collection db "theSimpsons")
(def coll (d/collection "theSimpsons"))
```
If we look at how the same would be done in java (from the example page)
```java
ArangoDB arangoDB = new ArangoDB.Builder().user("dev").password("123").build();
arangoDB.createDatabase("myDatabase");
arangoDB.db("myDatabase").createCollection("myCollection", null);
```
And in clojure
```clojure
(def conn (.build (-> (new ArangoDB$Builder)
                      (.user "dev")
                      (.password "123"))))
(-> conn
    (.createDatabase "myDatabase"))
(-> conn
    (.db "myDatabase")
    (.createCollection "myCollection"))
```
Using this library
```clojure
(def conn (arango/connect {:user "dev" :password "123"}))
(arango/create-database conn "myDatabase")
(-> conn
    (arango/db "myDatabase")
    (d/create-collection "myCollection"))
```

Includes internal serialization and deserialization from clojure collections to velocypack structures.

```
