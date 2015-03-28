# What's this?

ascii-hole lets you wire a simple menu to your app's STDIN.

My use case: I have an app that doesn't need a UI, but as I review its console logging statements, I would occasionally like to send commands to the app to reload the config or query the data.

# Visual example

[![asciicast](https://asciinema.org/a/12354.png)](https://asciinema.org/a/12354)

This example run was powered by the following snippet...

```clojure
(ns user
  (:require [ascii-hole.core :as ah]
            [clojure.core.async :refer [timeout]]))

(def counter (atom 0))

(defn reload-config
  "Pretend to reload a config but really just inc an atomic value."
  []
  (println "Reloading config!")
  (swap! counter inc))

(defn main []
  (ah/accept-keys {:key-map {:R #'reload-config}})
  (dotimes [n 5]
    (<!! (timeout 1000))
    (prn {:log-message n
          :config-reloaded-num-times @counter})))
```

# Usage
Pressing `?` displays a menu advertising the available keystrokes.

`:keymap` keystrokes can be specified as keywords, strings, ints, and char literals.
That is, the following snippets are equivalent...

```clojure
  (ascii-hole/accept-keys {:key-map {:R #'reload-config}})
```
```clojure
  (ascii-hole/accept-keys {:key-map {"R" #'reload-config}})
```
```clojure
  (ascii-hole/accept-keys {:key-map {82 #'reload-config}})
```
```clojure
  (ascii-hole/accept-keys {:key-map {\R #'reload-config}})
```

Nominal effort has been made to ensure non-printing control-character keystrokes print usefully in the help menu. Zero effort has been made to warn you about assigning keys that might be silly in a given context, like trying to assign `^S` or `^Z` while in BASH.

Many of the functions used in `ascii-hole.core` are overridable... perhaps too many.
