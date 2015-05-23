(ns tryclojure.models.eval
  (:require [clojail.testers :refer [secure-tester-without-def blanket]]
            [clojail.core :refer [sandbox]]
            [clojure.stacktrace :refer [root-cause]]
            [noir.session :as session]
            edn-ld.core
            edn-ld.demo)
  (:import java.io.StringWriter
           java.util.concurrent.TimeoutException))

(defn eval-form [form sbox]
  (with-open [out (StringWriter.)]
    (let [result (sbox form {#'*out* out})]
      {:expr form
       :result [out result]})))

(defn eval-string [expr sbox]
  (let [form (binding [*read-eval* false] (read-string expr))]
    (eval-form form sbox)))

(def try-clojure-tester
  (conj secure-tester-without-def (blanket "tryclojure" "noir")))

(defn make-sandbox []
  (sandbox try-clojure-tester
           :timeout 2000
           :init '(do (require '[clojure.repl :refer [doc source]]
                               '[clojure.string :as string]
                               '[edn-ld.core :refer :all]
                               '[edn-ld.common :refer :all]
                               '[edn-ld.demo :refer :all])
                      (future (Thread/sleep 600000)
                              (-> *ns* .getName remove-ns)))))

(defn find-sb [old]
  (if-let [sb (get old "sb")]
    old
    (assoc old "sb" (make-sandbox))))

(defn eval-request [expr]
  (try
    (eval-string expr (get (session/swap! find-sb) "sb"))
    (catch TimeoutException _
      {:error true :message "Execution Timed Out!"})
    (catch Exception e
      {:error true :message (str (root-cause e))})))
