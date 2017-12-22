(defproject ascii-hole "0.3.1"
  :description "Let console keystrokes asynchronously trigger specified CLJ/CLJS functions."
  :url "https://github.com/daemianmack/ascii-hole"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.3.465"]
                 [jline "2.11"]]
  :profiles {:dev {:source-paths ["dev"]
                   :aliases {"run-dev" ["trampoline" "run" "-m" "dev.user/-main"]}}})
