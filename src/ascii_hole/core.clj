(ns ascii-hole.core
  (:import [jline.console ConsoleReader])
  (:require [clojure.core.async :refer [go-loop]]
            [table.core :refer [table]]))


(declare print-keys)
(def ^:dynamic *debug*      true)
(def ^:dynamic *get-key-fn* nil)
(def ^:dynamic *on-key-fn*  nil)
(def ^:dynamic *key-map*    nil)
(def ^:dynamic *help-stub*  {\? #'print-keys})


(def is-help-key? #(= (-> *help-stub* keys first) %))

(defn print-keys
  "Print available keys, their associated functions and docstrings."
  [key-map]
  (table (for [[k v] (sort-by is-help-key? key-map)]
           {:key k :fn v :doc (some-> v meta :doc)})))


(defmulti  ->char (fn [k] (type k)))
(defmethod ->char clojure.lang.Keyword [k] (->char (name k)))
(defmethod ->char java.lang.String     [k] (->char (.charAt k 0)))
(defmethod ->char java.lang.Long       [k] (->char (char k)))
(defmethod ->char java.lang.Character  [k] k)

(defn char-keys [m] (zipmap (->> m keys (map ->char))
                            (->> m vals)))


(defn menu-map
  "Inject the help key into the key-map in such a way that the key can trigger
   a printout yet also show up as an available key in that printout."
  [key-map]
  (let [help-fn   (-> *help-stub* vals first)
        printable (merge *help-stub* key-map)  ;; "Dummy" reference to the print-key fn.
        do-print  #(help-fn printable)
        help-key  (-> *help-stub* keys first)]
    (merge {help-key do-print} key-map)))

(def announce-start #(do (println "Accepting keys!") (flush)))
(def inspect-stroke #(when *debug* (println (format "Accepting keystroke: '%c' (ASCII %d)" % (int %)))))
(def read-char #(let [cr (ConsoleReader.)] (char (.readCharacter cr))))
(defn eval-keyed-fn
  [key-map stroke]
  (inspect-stroke stroke)
  (when-let [requested-fn (get key-map stroke)] (requested-fn)))

(defn accept-keys
  ([] (accept-keys {}))
  ([{:keys [on-init-fn get-key-fn on-key-fn key-map]
     :or   {on-init-fn announce-start
            get-key-fn read-char
            on-key-fn  eval-keyed-fn} :as options}]
     (on-init-fn)
     (go-loop [the-char ((or *get-key-fn* get-key-fn))]
       (let [on-key-fn  (or *on-key-fn* on-key-fn)
             menu       (-> *key-map* (or key-map) char-keys menu-map)]
         (on-key-fn menu the-char))
       (recur ((or *get-key-fn* get-key-fn))))))
