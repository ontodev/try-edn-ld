(ns tryclojure.views.home
  (:require [hiccup.element :refer [javascript-tag link-to unordered-list]]
            [hiccup.page :refer [include-css include-js html5]]
            [hiccup.core :refer [html]]))

(defn links-html []
  (html
    (unordered-list
     [(link-to "https://github.com/ontodev/edn-ld" "EDN-LD code on GitHub")
      (link-to "http://clojure.org" "The official Clojure website")
      (link-to "http://clojure-doc.org/" "Clojure tutorials and documentation")
      (link-to "http://groups.google.com/group/clojure" "Clojure mailing list")
      ])))

(defn about-html []
  (html
    [:p.bottom
     "Welcome to Try EDN-LD - a quick tour of EDN-LD for absolute beginners."]
    [:p.bottom
     "Here is our only disclaimer: this site is an introduction to EDN-LD, not a generic Clojure REPL. "
     "You won't be able to do everything in it that you could do in your local interpreter. "
     "Also, the interpreter deletes the data that you enter if you define too many things, or after 15 minutes."]
    [:p.bottom
     "This site is based on "
     (link-to "http://tryclj.com" "TryClojure") ", "
     "which is written in Clojure and JavaScript with "
     (link-to "https://github.com/weavejester/compojure" "Compojure") ", "
     (link-to "https://github.com/noir-clojure/lib-noir" "lib-noir") ", "
     (link-to "https://github.com/flatland/clojail" "clojail") ", and Chris Done's "
     (link-to "https://github.com/chrisdone/jquery-console" "jquery-console") ". "
     " The design is by " (link-to "http://apgwoz.com" "Andrew Gwozdziewycz") "."]))

(defn home-html []
  (html
    [:p.bottom "Welcome to EDN-LD! "]
    [:p.bottom
     "You can see a Clojure interpreter above - we call it a <em>REPL</em>. "
     "If you aren't familiar with Clojure, "
     (link-to "http://tryclj.com" "TryClojure") "!"]
    [:p.bottom "Type <code>next</code> in the REPL to begin." ]))

(defn root-html []
  (html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (include-css "/css/tryclojure.css"
                 "/css/gh-fork-ribbon.css")
    (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"
                "/javascript/jquery-console/jquery.console.js"
                "/javascript/tryclojure.js")
    [:title "Try EDN-LD"]]
   [:body
    [:div#wrapper
      [:div.github-fork-ribbon-wrapper.right
       [:div.github-fork-ribbon
         (link-to "https://github.com/ontodev/try-edn-ld" "Fork me on GitHub")]]
     [:div#content
      [:div#header
       [:h1
        [:span.logo-clojure "Try EDN-LD!"]]]
      [:div#container
       [:div#console.console]
       [:div#buttons
        [:a#links.buttons "links"]
        [:a#about.buttons.last "about"]]
       [:div#changer (home-html)]
       [:div#hints
        [:p.bottom
         "Stuck? Try " [:code "restart"] ", " [:code "next"] ", "
         [:code "back"] ", " [:code "last"] ", or " [:code "goto #"]  "."]
        [:p.bottom
         "Or take a shortcut by using pre-defined vars like "
         [:code "_data"] " or " [:code "_triples"] "."]
        [:p.bottom
         "You can also try following the tutorial on your own system: "
         (link-to "https://github.com/ontodev/edn-ld"
                  "https://github.com/ontodev/edn-ld")]]]
      [:div.footer
       [:p.bottom "Based on " [:a {:href "http://tryclj.com"} "Try Clojure!"]]]
      (javascript-tag
       "var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-27340918-1']);
        _gaq.push(['_trackPageview']);

        (function() {
          var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
          ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
          var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();")]]]))

