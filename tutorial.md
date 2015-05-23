# Introduction

I'll take you on a 5-minutes tour of EDN-LD, but feel free to experiment on your own along the road!

You can type `next` to skip forward, `back` to return to the previous step, and `restart` to get back to the beginning. Let's get started: type `next`.


# Tables to Triples

Say we have a (very small) table of books and their authors called `books.tsv`:

Title     | Author
----------|-------
The Iliad | Homer

We want to turn it into a linked data file like `books.ttl`:

    @prefix dc:    <http://purl.org/dc/elements/1.1/> .
    @prefix ex:    <http://example.com/> .
    @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
    @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

    ex:the-iliad  a    ex:book ;
            dc:author  ex:Homer ;
            dc:title   "The Iliad"^^xsd:string .


# Tables to EDN

A common way to represent tables in Clojure is as a list of maps, with the column names as the keys. Try this:

    > tsv
    > (def lines (string/split-lines tsv))
    > lines
    > (defn split-tabs [s] (string/split s #"\t"))
    > (def table (map split-tabs lines))
    > table
    > (def rows (drop 1 table))
    > rows

Now we use `zipmap` to associate keys with values:

    > (def data (map (partial zipmap [:title :author]) rows))
    > data


# Names

We have the data in a convenient shape, but what does it mean? Well, there's some resource that has "The Iliad" as its title, and some guy named "Homer" who is the author of that resource. We also know from the context that it's a book.

The first thing to do is give names to our resources. Linked data names are [IRIs](https://en.wikipedia.org/wiki/Internationalized_resource_identifier): globally unique identifiers that generalize the familiar URL you see in your browser's location bar. We can use some standard names for our relations from the [Dublin Core](http://dublincore.org) metadata standard, and we'll make up some more.

Name      | IRI
----------|-----------------------------------------
title     | `http://purl.org/dc/elements/1.1/title`
author    | `http://purl.org/dc/elements/1.1/author`
The Iliad | `http://example.com/the-iliad`
Homer     | `http://example.com/Homer`
book      | `http://example.com/book`


# Prefixes

IRIs can be long and cumbersome, so let's define some prefixes that we can use to shorten them:

Prefix | IRI
-------|-----------------------------------
`dc`   | `http://purl.org/dc/elements/1.1/`
`ex`   | `http://example.com/`

The `ex` prefix will be our default. We use strings for full IRIs and keywords when we're using some sort of contraction.

IRI                                      | Contraction
-----------------------------------------|------------
`http://purl.org/dc/elements/1.1/title`  | `:dc:title`
`http://purl.org/dc/elements/1.1/author` | `:dc:author`
`http://example.com/the-iliad`           | `:the-iliad`
`http://example.com/Homer`               | `:Homer`
`http://example.com/book`                | `:book`


# Context

We'll put this naming information in a *context* map:

    > (def context {:dc "http://purl.org/dc/elements/1.1/", :ex "http://example.com/", nil :ex, :title :dc:title, :author :dc:author})

The `nil` key indicates the default prefix `:ex`. Now we can use the context to expand contractions and to contract IRIs:

    > (expand context :title)
    > (expand context :Homer)
    > (contract context "http://purl.org/dc/elements/1.1/title")
    > (contract context "http://purl.org/dc/elements/1.1/foo")
    > (expand-all context data)


# Resources

Sometimes we also want to *resolve* a name to an IRI. We can define a resources map from string to IRIs or contractions:

    > (def resources {"Homer" :Homer, "The Iliad" :the-iliad})
    > resources

We should include this information in our data by assigning a special `:subject-iri` to each of our maps. We can do this one at a time with `assoc`:

    > (def book (assoc (first data) :subject-iri :the-iliad))
    > book

Or we can use a higher-order function to find the title from the resources map:

    > (def books (mapv #(assoc % :subject-iri (get resources (:title %))) data))
    > books


# Triples

Now it's time to convert our book data to "triples", i.e. statements about things to put in our graph. A triple consists of a subject, a predicate, and an object:

- the subject is the name of a resource: an IRI
- the predicate is the name of a relation: also an IRI
- the object can either be an IRI or literal data.


# Literal Data

We represent an IRI with a string, or a contracted IRI with a keyword. We represent literal data as a map with special keys:

- `:value` is the string value ("lexical value") of the data, e.g. "The Iliad", "100.31"
- `:type` is the IRI of a data type, with `xsd:string` as the default
- `:lang` is an optional language code, e.g. "en", "en-uk"

The `literal` function is a convenient way to create a literal map:

    > (literal "The Iliad")
    > (literal 100.31)

The `objectify` function takes a resource map and a value, and determines whether to convert the value to an IRI or a literal:

    > (objectify resources "Some string")
    > (objectify resources "Homer")


# Triplify

Now we can treat each map as a set of statements about a resources, and `triplify` it to a lazy sequence of triples. The format will be "flat triples", a list with slots for: subject, predicate, object, type, and lang.

The `triplify` function takes our resource map and a map of data that includes a `:subject-iri` key. It returns a lazy sequence of triples.

    > (def triples (triplify resources book))
    > (vec triples)


# Graphs

You'll notice that the subject `:the-iliad` is repeated here. With a larger set of triples the redundancy will be greater. Instead we can use a nested data structure:

    > (def subjects (subjectify triples))
    > subjects

From the inside out, it works like this:

- object-set: the set of object with the same subject and predicate
- predicate-map: a map from predicate IRIs to object sets
- subject-map: map from subject IRIs to predicate sets

We work with these data structures like any other Clojure data, using `merge`, `assoc`, `update`, `conj`, and the rest of the standard Clojure toolkit:

    > (def context+ (merge default-context context))
    > (def subjects+ (assoc-in subjects [:the-iliad :rdf:type] #{:book}))
    > (def triples+ (conj triples [:the-iliad :rdf:type :book]))

Now, we can write to standard linked data formats, such as Turtle:

    > (def prefixes (assoc (get-prefixes context) :rdf rdf :xsd xsd))
    > (def expanded-triples (map #(expand-all context+ %) triples+))
    > (write-triples "books.ttl" prefixes expanded-triples)

**Oops!** This tutorial runs inside a secure "sandbox", and you can't write files from inside the sandbox.


# Named Graphs

One more thing before we're done: *named graphs*. A graph is just a set of triples. When we want to talk about a particular graph, we give it a name: an IRI, of course. Then we can talk about sets of named graphs when we want to compare them, merge them, etc. The official name for a set of graphs is an "[RDF dataset](http://www.w3.org/TR/rdf11-concepts/#section-dataset)". A dataset includes "default graph" with no name.

By adding the name of a graph, our *triples* become *quads* ("quadruples"). We define a quad and some new functions to handle them.

    > (def library [(assoc book :graph-iri :library)])
    #'user/library
    > library
    [{:title "The Iliad", :author "Homer", :subject-iri :the-iliad, :graph-iri :library}]
    > (def quads (quadruplify-all resources library))
    #'user/quads
    > (vec quads)
    [[:library :the-iliad :title {:value "The Iliad"}] [:library :the-iliad :author :Homer]]
    > (graphify quads)
    {:library {:the-iliad {:title #{{:value "The Iliad"}}, :author #{:Homer}}}}

Note that RDFXML format doesn't support named graphs and quads.


# More EDN-LD

Thanks for making it to the end of this interative tutorial!

Learn more about EDN-LD at our GitHub page: <https://github.com/ontodev/edn-ld>.

