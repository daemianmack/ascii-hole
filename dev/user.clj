(ns dev.user
  (:require [ascii-hole.core :as ah]
            [clojure.core.async :refer [<!! timeout]]))

(def state (atom {:n 50
                  :text nil
                  :pause false}))

(defn inc-n
  "Increment a counter."
  []
  (swap! state update :n inc))

(defn dec-n
  "Decrement a counter."
  []
  (swap! state update :n dec))

(defn store-text
  "Pause printing to the screen, accept a line of input, and store
  that in the state. Finally, resume printing."
  []
  (swap! state assoc :pause true)
  (ah/accept-line "Store some text: "
                  (fn [input]
                    (swap! state assoc
                           :text input
                           :pause false))))

(defn -main []
  (ah/accept-keys {:key-map {:plus #'inc-n
                             :dash #'dec-n
                             :enter {:fn #'store-text
                                     :doc "Store some text."}
                             :p {:fn #(prn "Hi, it's me, a lambda")
                                 :fn-name 'demonstrate-lambda
                                 :doc "Demonstrate a friendly lambda."}}})
  (loop [{:keys [n text pause]} @state]
    (if pause
      (do (<!! (timeout 100))
          (recur @state))
      (if (pos? n)
        (do (dec-n)
            (prn (dissoc @state :pause))
            (<!! (timeout 1000))
            (recur @state))
        (do (prn :done)
            (System/exit 0))))))
