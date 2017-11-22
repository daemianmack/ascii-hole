(ns ascii-hole.core
  (:import #?(:clj [jline.console ConsoleReader]))
  (:require [clojure.string :as s]
            [ascii-hole.keycodes :as keycodes]
            #?(:clj [clojure.core.async :as a])
            #?(:clj  [clojure.pprint :refer [print-table]]
               :cljs [cljs.pprint    :refer [print-table]])))

(def global-key-map (atom nil))

#?(:cljs (def line-listener (atom identity)))

#?(:cljs (def stdin (.-stdin js/process)))
#?(:cljs (def readline (js/require "readline")))
#?(:cljs (.emitKeypressEvents readline stdin))

#?(:clj (def cr (ConsoleReader.)))


(declare print-keys)
(def ^:dynamic *debug*      false)
(def ^:dynamic *help-stub*  {:? #'print-keys})


(def is-help-key? #(= (-> *help-stub* keys first) %))

(defn print-keys
  "Print available keys, their associated functions and docstrings."
  [key-map]
  (print-table (for [[k v] (sort-by is-help-key? key-map)
                     :let [fn  (or (:fn-name v) (:fn v) v)
                           doc (or (:doc v) (some-> v meta :doc))]]
                 {"key" (name k)
                  "fn"  fn
                  "doc" doc})))

(defn menu-map
  "Inject the help key into the key-map in such a way that the key can trigger
   a printout yet also show up as an available key in that printout."
  [key-map]
  (let [help-fn   (-> *help-stub* vals first)
        printable (merge *help-stub* key-map)  ;; "Dummy" reference to the print-key fn.
        do-print  #(help-fn printable)
        help-key  (-> *help-stub* keys first)]
    (merge {help-key do-print} key-map)))

(def announce-start #(do (println "Accepting keys! Press"
                                  (-> *help-stub* keys first name)
                                  "for help.")
                         (flush)))

(def inspect-stroke
  #(when *debug*
     (println (str "Accepting keystroke: " %))))

(defn eval-keyed-fn
  [stroke]
  (inspect-stroke stroke)
  (let [menu (menu-map @global-key-map)]
    (when-let [requested-fn (get-in menu [stroke :fn]
                                    (get menu stroke))]
      (requested-fn))))


#?(:clj
   (defn read-one-line [prompt cb]
     (let [input (.readLine cr prompt)]
       (cb input))))

#?(:clj
   (defn read-one-key []
     (-> cr .readCharacter keycodes/by-int)))


#?(:cljs
   (declare listen-for-key))

#?(:cljs
   (defn read-one-line [cb input-buffer]
     (cb (s/trim (.toString input-buffer "utf-8")))
     (listen-for-key)))

#?(:cljs
   (declare read-one-key))

#?(:cljs
   (defn listen-for-line [prompt cb]
     (.removeListener stdin "keypress" read-one-key)
     (.setRawMode stdin false)
     (print prompt)
     (let [lfn (partial read-one-line cb)]
       (reset! line-listener lfn)
       (.on stdin "data" lfn))))

#?(:cljs
   (defn read-one-key [str key]
     (let [k (js->clj key :keywordize-keys true)]
       (when-let [keystroke (keycodes/by-str k)]
         ;; JS STDIN's .setRawMode is necessary to trap single
         ;; keypresses but has the side-effect of swallowing all
         ;; input -- notably, control characters -- thus we have
         ;; to detect Ctrl+C and exit manually.
         (when (= :ctrl_c keystroke)
           (.exit js/process 130))
         (eval-keyed-fn keystroke)))))

#?(:cljs
   (defn listen-for-key []
     (.setRawMode stdin true)
     (.removeListener stdin "data" @line-listener)
     (reset! line-listener nil)
     (.on stdin "keypress" read-one-key)))


(defn accept-keys
  ([] (accept-keys {}))
  ([{:keys [on-init-fn key-map]
     :or   {on-init-fn announce-start} :as options}]
   (on-init-fn)
   (reset! global-key-map key-map)
   ;; JVM/JS differences:
   ;; - JS has no proper Chars, only Strings which satisfy `char?` if
   ;;   one character in length.
   ;; - Would prefer these two forms share an implementation or even
   ;;   similar shape, but non-JVM core.async appears to require
   ;;   mfike's andare, requiring which causes a 50-second load delay.
   #?(:clj (a/go-loop [the-char (read-one-key)]
             (eval-keyed-fn the-char)
             (recur (read-one-key)))
      :cljs (listen-for-key))))
