# clj-arangodb

[![Clojars Project](https://img.shields.io/clojars/v/beoliver/clj-arangodb.svg)](https://clojars.org/beoliver/clj-arangodb)

Arangodb is a multi-modal database.

The maintainers of arangodb provide a java driver for communicating with an arangodb server.
This library provides clojure developers a thin (and incomplete) wrapper of that interface.
Much like monger, the java implementation is still visible.

If we look at how the java code is used, in this example a new connecton is being made to a server.
```java
ArangoDB arangoDB = new ArangoDB.Builder().useProtocol(Protocol.VST).host("192.168.182.50", 8888).build();
```
In the clojure version we are doing exactly the same thing under the hood.
```clojure
(def arango-db (connect {:useProtocol Protocol/VST :host ["192.168.182.50" 8888]}))
```
The keys used in option maps identical to the methods of the java-driver. This makes looking up information nice and easy.
By default the java driver connectes to arangodb using something called a velocystream.
this has some implications - firstly it is in theory more efficient, secondly we need to be aware of something called velocypacks - but we will come to that later.

in general functions that take options expect Arango option objects -
Have a look in the options namespace for the conversion you need.
If you're using emacs you will get feedback which keys each ooptions builder expects.

for example if you wanted to create document read options then use `map->DocumentReadOptions`
```clojure
(defn ^DocumentReadOptions map->DocumentReadOptions
  [{:keys [ifNoneMatch ifMatch catchException] :as options}]
  (option-builder (new DocumentReadOptions) options))
```
Even through you could pass a load of junk to the function - if you are using emacs you can see what keys are accepted.

This also means that if you want to pass options for a method that I have not implemented, you can just create the object (and make a pull request :P)


# Creating Documents

To create documents you need to convert them to either a vpack slice or a json string.
two functions are provided in `clj-arangodb.velocypack.core` - nameley `pack` and `unpack`.


The function `unpack` takes an optional key conversion function - by default `(unpack s)` is the same as `(unpack s keyword)` - if you want strings as keys then you should use the following `(unpack s identity)`.

To get a document you can use `get-document` functions in `databases` (by id) or `collection` (by key).

The possible return types are `String`, `VpackSlice` and `BaseDocument`
The helper function `get-document-as-map` gets the document as a VpackSlice then converts it using `upack` - again a `key-fn` can be passed.