(ns dev.user
  (:require [ascii-hole.core :as ah]))

(def state (atom {:n 100
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

(defn inspect-state []
  (let [{:keys [n text pause]} @state]
    (cond
      pause    (js/setTimeout inspect-state 100)
      (pos? n) (do (dec-n)
                   (prn (dissoc @state :pause))
                   (js/setTimeout inspect-state 1000))
      :else    (do (prn :done)
                   (.exit js/process 0)))))

(defn store-text
  "Pause printing to the screen, accept a line of input, and store
  that in the state. Finally, resume printing."
  []
  (swap! state assoc :pause true)
  (ah/accept-line "Store some text: "
                  (fn [res]
                    (swap! state assoc
                           :text res
                           :pause false))))

(defn -main []
  (ah/accept-keys {:key-map {:plus #'inc-n
                             :dash #'dec-n
                             :enter {:fn #'store-text
                                     :doc "Store some text."}
                             :p {:fn #(prn "Hi, it's me, a lambda")
                                 :fn-name 'demonstrate-lambda
                                 :doc "Demonstrate a friendly lambda."}}})
  (inspect-state))



;; lumo -c src:dev -m dev.user