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

`:keymap` keystrokes can be specified using somewhat arbitrary [human keyword names].

[human keyword names]: src/ascii_hole/keycodes.cljc#L22

```clojure
  ;; Accept keypress of `R`.
  (ascii-hole/accept-keys {:key-map {:R #'reload-config}})
```
```clojure
  ;; Accept keypress of `Ctrl+r`.
  (ascii-hole/accept-keys {:key-map {:ctrl_r #'reload-config}})
```
A var specifies a callable function.

The above demonstrates the general case for configuration, but you may
want to display a friendlier name or include a line of documentation
to the help menu. Also, in ClojureScript, lambdas will print terribly,
so, `accept-keys` will also accept a map offering more control over
menu printout...

```
(ascii-hole/accept-keys {:key-map {:p {:fn #(prn "Hi, it's me, a lambda.")
                                       :fn-name 'demonstrate-lambda
                                       :doc "Demonstrate a friendly lambda."}}})

```

In addition to `accept-keys`, which traps single keypresses, there's
an `accept-line` function which expects a text prompt and a callback
function. When triggered, it will print the prompt and accept input,
terminating on an EOF, at which point the input will be passed to the
callback for the consuming program to handle. [Example code]

[Example code]: dev/user.cljs#L32-36

[![asciicast](https://asciinema.org/a/VPnXjh6zJFZwyCsCj4yQxTR5h.png)](https://asciinema.org/a/VPnXjh6zJFZwyCsCj4yQxTR5h)



# ClojureScript support

AsciiHole's ClojureScript support targets [Lumo]. Expanding it to
support [Planck] is possible but unplanned.

[Lumo]:https://github.com/anmonteiro/lumo
[Planck]:https://github.com/mfikes/planck


