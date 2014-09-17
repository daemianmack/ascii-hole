(defproject ascii-hole "0.1.0-SNAPSHOT"
  :description "Let console keystrokes asynchronously trigger specified functions."
  :url "https://github.com/daemianmack/ascii-hole"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [jline "2.11"]]
  :profiles {:dev {:source-paths ["dev"]
                   :aliases {"run-dev" ["trampoline" "run" "-m" "user/main"]}}}
  :bootclasspath true)
