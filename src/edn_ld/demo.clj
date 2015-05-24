(ns edn-ld.demo
  (:require [clojure.string :as string]
            [edn-ld.core :as ld]
            [edn-ld.common :refer :all]
            [edn-ld.jena :as jena]))

(defn to-identifier
  "Take a string and return a properly formatted identifier string."
  [s]
  (-> s
      string/trim
      string/lower-case
      (string/replace #"(\W|_|/)+" "-")))

(defn to-keyword
  "Take a string and return a properly formatted keyword."
  [s]
  (keyword (to-identifier s)))

(defn split-tsv
  [s]
  (->> s
       string/split-lines
       (map #(string/split % #"\t"))))

(defn read-tsv
  "Given a path to a tab-separated values file,
   return a lazy sequence of rows, each a sequence of cells."
  [path]
  (->> path
       slurp
       string/split-lines
       (map #(string/split % #"\t"))))

(defn map-headers
  "Given a sequence of rows where the first row is the column headers,
   return a sequence of maps with column keywords as keys."
  [[headers & data]]
  (map (partial zipmap (map to-keyword headers)) data))

(def books-path "resources/public/books.tsv")

(def tsv (slurp books-path))

(def _table (split-tsv tsv))

(def _rows (map-headers _table))

(def _data (map (partial zipmap [:title :author]) _rows))

(def _context
  {:dc     "http://purl.org/dc/elements/1.1/"
   :ex     "http://example.com/"
   nil     :ex
   :title  :dc:title
   :author :dc:author})

(def _resources
  {"The Iliad" :the-iliad
   "Homer"     :Homer})

(def _book (assoc (first _data) :subject-iri :the-iliad))

(def _books (map #(assoc % :subject-iri (get _resources (:title %))) _rows))

(def _triples (ld/triplify-all _resources _books))

(def _subjects (ld/subjectify _triples))

(def _context+ (merge default-context _context))

(def _subjects+ (assoc-in _subjects [:the-iliad :rdf:type] #{:book}))

(def _triples+ (conj _triples [:the-iliad :rdf:type :book]))

(def _prefixes (assoc (ld/get-prefixes _context) :rdf rdf :xsd xsd))

(def _expanded-triples (map #(ld/expand-all _context+ %) _triples+))

(def read-triple-string jena/read-triple-string)

(defn write-triple-string
  "Given an optional format, optional base, optional prefixes, and Triples,
   return a string representation."
  ([triples]
   (write-triple-string default-prefixes triples))
  ([prefixes triples]
   (write-triple-string nil prefixes triples))
  ([format prefixes triples]
   (write-triple-string format nil prefixes triples))
  ([format base prefixes triples]
   (with-open [writer (java.io.StringWriter.)]
     (.write (jena/get-model prefixes triples) writer format base)
     (str writer))))

; (write-triple-string _prefixes _expanded-triples)
