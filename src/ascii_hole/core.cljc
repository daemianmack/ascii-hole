(ns ascii-hole.core
  (:import #?(:clj [jline.console ConsoleReader]))
  (:require [clojure.string :as s]
            [ascii-hole.keycodes :as keycodes]
            #?(:clj [clojure.core.async :as a])
            #?(:clj  [clojure.pprint :refer [print-table]]
               :cljs [cljs.pprint    :refer [print-table]])))


#?(:cljs
   (def rl (js/require "readline")))
#?(:cljs
   (def term (.terminal (js/require "terminal-kit"))))

#?(:clj
   (def cr (ConsoleReader.)))


(def global-key-map (atom nil))

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
                 {:key (name k)
                  :fn fn
                  :doc doc})))

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
   (defn read-char []
     (-> cr .readCharacter
         ((fn [x] (prn :raw-char x) x))
         keycodes/by-int)))

#?(:cljs
   (defn process-key
     [the-char key]
     ;; e.g. arrowkeys issue a key but not a char.
     (when-let [the-char (keycodes/by-str the-char)]
       ;; JS STDIN's .setRawMode is necessary to trap single
       ;; keypresses but has the side-effect of swallowing all
       ;; input -- notably, control characters -- thus we have
       ;; to detect Ctrl+C and exit manually.
       (when (= :ctrl_c the-char)
         (.exit js/process 130))
       (eval-keyed-fn the-char))))

#?(:cljs
   (defn read-one-line [prompt cb]
     (.removeListener term "key" process-key)
     (.grabInput term false)
     (.green term prompt)
     (.inputField term (clj->js {:echo true
                                 :cancelable true})
                  (fn [err res]
                    (cb err res)
                    (.on term "key" process-key)))))

#?(:cljs
   (defn read-one-key []
     (.grabInput term true)
     (.on term "key" process-key)))

(defn accept-keys
  ([] (accept-keys {}))
  ([{:keys [on-init-fn key-map]
     :or   {on-init-fn announce-start} :as options}]
   (on-init-fn)
   ;; JVM/JS differences:
   ;; - JS has no proper Chars, only Strings which satisfy `char?` if
   ;;   one character in length.
   ;; - Would prefer these two forms share an implementation but
   ;;   non-JVM core.async requires mfike's andare, requiring which
   ;;   causes a 20-second load delay.
   #?(:clj (do (reset! global-key-map key-map)
               (a/go-loop [the-char (read-char)]
                 (when *debug* (prn :the-char the-char))
                 (eval-keyed-fn the-char)
                 (recur (read-char))))
      :cljs (do (reset! global-key-map key-map)
                (read-one-key)))))
