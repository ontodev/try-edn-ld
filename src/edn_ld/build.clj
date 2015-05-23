(ns edn-ld.build
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh with-sh-dir]]))

(def resources-path "resources/public/")
(def tutorial-path  (str resources-path "tutorial/"))
(def javascript-path (str resources-path "javascript/tryclojure.js"))

(defn page-list
  "Given a page count, return a string defining a JavaScript array of pages."
  [x]
  (str "var pages = [\n"
       (string/join
        ",\n"
        (for [i (range 1 (inc x))]
          (format "  \"page%d\"" i)))
       "\n];\n"))

(defn fix-javascript
  "Delete the first several lines of the JavaScript file,
   and replace them with a new pages array."
  [x]
  (->> javascript-path
       slurp
       string/split-lines
       (drop-while #(not= "// START" %))
       (concat [(page-list x)])
       (string/join "\n")
       (spit javascript-path)))

(defn call-pandoc
  "Given a Markdown string,
   call Pandoc in the shell and return the result."
  [md]
  (:out (sh "pandoc"
            "--from" "markdown+pipe_tables"
            "--to"   "html"
            :in      md)))

(defn build-page
  "Given a number and some Markdown,
   write an HTML page fragment."
  [index md]
  (->> md
       (str "## ")
       call-pandoc
       (spit (format "%spage%d.html" tutorial-path (inc index)))))

(defn build-pages
  "Given a path to a Markdown file,
   delete all the existing tutorial files,
   then build new ones."
  [path]
  (doseq [file (.listFiles (io/file tutorial-path))]
    (.delete file))
  (->> (string/split (slurp path) #"(?m)^#+\s+")
       (remove string/blank?)
       (map-indexed build-page)
       count
       fix-javascript
       doall))

(defn -main
  [& args]
  (build-pages "tutorial.md"))
