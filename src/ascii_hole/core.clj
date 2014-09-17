(ns ascii-hole.core
  (:import [jline.console ConsoleReader])
  (:require [clojure.core.async :refer [go-loop put! take! timeout]]))


(def ^:dynamic *debug*      nil)
(def ^:dynamic *get-key-fn* nil)
(def ^:dynamic *on-key-fn*  nil)
(def ^:dynamic *key-map*    nil)


(def announce-start #(do (println "Listening for keystrokes!") (flush)))
(def inspect-stroke #(when *debug* (println (format "Keystroke %d ('%c')." (int %) %))))
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
             key-map    (or *key-map*    key-map)]
         (on-key-fn key-map the-char))
       (recur ((or *get-key-fn* get-key-fn))))))
