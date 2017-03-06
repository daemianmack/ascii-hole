(ns ascii-hole.core
  (:import #?(:clj [jline.console ConsoleReader]))
  (:require #?(:clj [clojure.core.async :as a])
            #?(:clj [table.core :refer [table]])))


#?(:cljs
   (def rl (js/require "readline")))

#?(:clj
   (defn read-char []
     (let [cr (ConsoleReader.)] (char (.readCharacter cr)))))


(declare print-keys)
(def ^:dynamic *debug*      true)
(def ^:dynamic *help-stub*  {\? #'print-keys})


(def is-help-key? #(= (-> *help-stub* keys first) %))

(defn ->int [c]
  #?(:clj (int c)
     :cljs (.charCodeAt c 0)))

(defn mk-printable
  [k]
  (if (< 0 (->int k) 26)
    (str "^" (-> k ->int (+ 64) char))
    k))

(defn print-keys
  "Print available keys, their associated functions and docstrings."
  [key-map]
  #?(:clj (table (for [[k v] (sort-by is-help-key? key-map)]
                   {:key (mk-printable k) :fn v :doc (some-> v meta :doc)}))
     :cljs (print key-map)))

(defn ->char [x]
  (cond
     (char?    x) x
     (keyword? x) (->char (name x))
     (string?  x) (->char (.charAt x 0))
     (number?  x) (->char (char x))))

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

(def inspect-stroke
  #(when *debug*
     (println (str "Accepting keystroke: '" (mk-printable %) "' (ASCII " (->int %) ")"))))

(defn eval-keyed-fn
  [key-map stroke]
  (inspect-stroke stroke)
  (when-let [requested-fn (get key-map stroke)] (requested-fn)))

(defn accept-keys
  ([] (accept-keys {}))
  ([{:keys [on-init-fn key-map]
     :or   {on-init-fn announce-start} :as options}]
   (on-init-fn)
   ;; JVM/JS differences:
   ;; - JS has no proper Chars, only Strings which satisfy `char?` if
   ;;   one character in length.
   ;; - Would prefer these two forms share an implementation or even
   ;;   similar shape, but non-JVM core.async appears to require
   ;;   mfike's andare, requiring which causes a 50-second load delay.
   #?(:clj (a/go-loop [the-char (read-char)]
             (let [menu (-> key-map char-keys menu-map)]
               (eval-keyed-fn menu the-char))
             (recur (read-char)))
      :cljs (do (.emitKeypressEvents rl (.-stdin js/process))
                (.setRawMode (.-stdin js/process) true)
                (.on (.-stdin js/process) "keypress"
                     (fn [the-char key]
                       ;; e.g. arrowkeys issue a key but not a char.
                       (when the-char
                         ;; JS STDIN's .setRawMode is necessary to
                         ;; trap single keypresses but has the
                         ;; side-effect of treating control characters
                         ;; unspecially, thus we have to detect Ctrl+C
                         ;; and exit manually.
                         (when (= 3 (->int the-char))
                           (.exit js/process 1))
                         (let [menu (-> key-map char-keys menu-map)]
                           (eval-keyed-fn menu the-char)))))))))
