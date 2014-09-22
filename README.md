# What's this?

ascii-hole lets you wire a simple menu to your app's STDIN.

My use case: I have an app that doesn't need a UI, but as I review its console logging statements, I would occasionally like to send commands to the app to reload the config or query the data.

# Visual example

See here: https://asciinema.org/a/12354

This example run was powered by the following snippet...

```clojure
(ns user
  (:require [ascii-hole.core :as ascii-hole]
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
Many of the functions used in `ascii-hole.core` are overridable... perhaps too many.

Keystrokes can be specified as keywords, strings, ints, and char literals.
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

Specify non-printing control characters with char literals and ints.
These are equivalent...

```clojure
  (ascii-hole/accept-keys {:key-map {\^D #'reload-config}})
```
```clojure
  (ascii-hole/accept-keys {:key-map {4 #'reload-config}})
```


