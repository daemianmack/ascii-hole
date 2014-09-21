(ns ascii-hole.core
  (:import [jline.console ConsoleReader])
  (:require [clojure.core.async :refer [go-loop put! take!]]))


(def ^:dynamic *debug*      true)
(def ^:dynamic *get-key-fn* nil)
(def ^:dynamic *on-key-fn*  nil)
(def ^:dynamic *key-map*    nil)


(defmulti  ->char (fn [k] (type k)))
(defmethod ->char clojure.lang.Keyword [k] (->char (name k)))
(defmethod ->char java.lang.String     [k] (->char (.charAt k 0)))
(defmethod ->char java.lang.Long       [k] (->char (char k)))
(defmethod ->char java.lang.Character  [k] k)

(defn char-keys [m] (zipmap (->> m keys (map ->char))
                            (->> m vals)))

(def announce-start #(do (println "Accepting keys!") (flush)))
(def inspect-stroke #(when *debug* (println (format "Accepting keystroke: %c ('%d')" % (int %)))))
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
       (let [on-key-fn  (or *on-key-fn*  on-key-fn)
             key-map    (char-keys (or *key-map* key-map))]
         (on-key-fn key-map the-char))
       (recur ((or *get-key-fn* get-key-fn))))))
