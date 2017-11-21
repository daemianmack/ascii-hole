(ns ascii-hole.keycodes
  (:require [clojure.set :refer [map-invert]]))


;; Provide a mapping to canonical names.
;; JVM's ConsoleReader exposes keypresses as chars.
;; JS exposes them as strings and maps of data, e.g.,
;;   #js {:sequence "|", :name nil, :ctrl false, :meta false, :shift false}
;; but these seem inconsistent.
;; npm's `terminal-kit` provides a convenience layer over those maps by
;; assigning names and correcting those inconsistencies.

;; NB: Not all keystrokes are available in both environments, and some
;; have meaning at the system level (e.g., ^\).
(def canonical-mapping
  {:ctrl_a {:int 1 :str "CTRL_A"}
   :ctrl_b {:int 2 :str "CTRL_B"}
   :ctrl_c {:int 3 :str "CTRL_C"}
   :ctrl_d {:int 4 :str "CTRL_D"}
   :ctrl_e {:int 5 :str "CTRL_E"}
   :ctrl_f {:int 6 :str "CTRL_F"}
   :ctrl_g {:int 7 :str "CTRL_G"}
   :ctrl_h {:int 8 :str "BACKSPACE"}
   :ctrl_i {:int 9 :str "TAB"}
   :ctrl_j {:int 10 :str "CTRL_J"}
   :ctrl_k {:int 11 :str "CTRL_K"}
   :ctrl_l {:int 12 :str "CTRL_L"}
   :ctrl_m {:int 13 :str "ENTER"}
   :ctrl_n {:int 14 :str "CTRL_N"}
   :ctrl_o {:int 15 :str "CTRL_O"}
   :ctrl_p {:int 16 :str "CTRL_P"}
   :ctrl_q {:int 17 :str "CTRL_Q"}
   :ctrl_r {:int 18 :str "CTRL_R"}
   :ctrl_s {:int 19 :str "CTRL_S"}
   :ctrl_t {:int 20 :str "CTRL_T"}
   :ctrl_u {:int 21 :str "CTRL_U"}
   :ctrl_v {:int 22 :str "CTRL_V"}
   :ctrl_w {:int 23 :str "CTRL_W"}
   :ctrl_x {:int 24 :str "CTRL_X"}
   :ctrl_y {:int 25 :str "CTRL_Y"}
   :ctrl_z {:int 26 :str "CTRL_Z"}
   :escape {:int 27 :str "ESCAPE"} ;; ^[
   ;; :28 {:int 28 :str ""}
   :29 {:int 29 :str ""}
   :30 {:int 30 :str ""}
   :31 {:int 31 :str ""}
   :space {:int 32 :str " "}
   :bang {:int 33 :str "!"}
   :quote {:int 34 :str "\""}
   :hash {:int 35 :str "#"}
   :dollar {:int 36 :str "$"}
   :percent {:int 37 :str "%"}
   :ampersand {:int 38 :str "&"}
   :tick {:int 39 :str "'"}
   :left-paren {:int 40 :str "("}
   :right-paren {:int 41 :str ")"}
   :asterisk {:int 42 :str "*"}
   :plus {:int 43 :str "+"}
   :comma {:int 44 :str ","}
   :dash {:int 45 :str "-"}
   :dot {:int 46 :str "."}
   :slash {:int 47 :str "/"}
   :0 {:int 48 :str "0"}
   :1 {:int 49 :str "1"}
   :2 {:int 50 :str "2"}
   :3 {:int 51 :str "3"}
   :4 {:int 52 :str "4"}
   :5 {:int 53 :str "5"}
   :6 {:int 54 :str "6"}
   :7 {:int 55 :str "7"}
   :8 {:int 56 :str "8"}
   :9 {:int 57 :str "9"}
   :colon {:int 58 :str ":"}
   :semicolon {:int 59 :str ";"}
   :less-than {:int 60 :str "<"}
   :equal {:int 61 :str "="}
   :greater-than {:int 62 :str ">"}
   :? {:int 63 :str "?"}
   :at {:int 64 :str "@"}
   :A {:int 65 :str "A"}
   :B {:int 66 :str "B"}
   :C {:int 67 :str "C"}
   :D {:int 68 :str "D"}
   :E {:int 69 :str "E"}
   :F {:int 70 :str "F"}
   :G {:int 71 :str "G"}
   :H {:int 72 :str "H"}
   :I {:int 73 :str "I"}
   :J {:int 74 :str "J"}
   :K {:int 75 :str "K"}
   :L {:int 76 :str "L"}
   :M {:int 77 :str "M"}
   :N {:int 78 :str "N"}
   :O {:int 79 :str "O"}
   :P {:int 80 :str "P"}
   :Q {:int 81 :str "Q"}
   :R {:int 82 :str "R"}
   :S {:int 83 :str "S"}
   :T {:int 84 :str "T"}
   :U {:int 85 :str "U"}
   :V {:int 86 :str "V"}
   :W {:int 87 :str "W"}
   :X {:int 88 :str "X"}
   :Y {:int 89 :str "Y"}
   :Z {:int 90 :str "Z"}
   :left-bracket {:int 91 :str "["}
   :back-slash {:int 92 :str "\\"}
   :right-bracket {:int 93 :str "]"}
   :caret {:int 94 :str "^"}
   :underscore {:int 95 :str "_"}
   :backtick {:int 96 :str "`"}
   :a {:int 97 :str "a"}
   :b {:int 98 :str "b"}
   :c {:int 99 :str "c"}
   :d {:int 100 :str "d"}
   :e {:int 101 :str "e"}
   :f {:int 102 :str "f"}
   :g {:int 103 :str "g"}
   :h {:int 104 :str "h"}
   :i {:int 105 :str "i"}
   :j {:int 106 :str "j"}
   :k {:int 107 :str "k"}
   :l {:int 108 :str "l"}
   :m {:int 109 :str "m"}
   :n {:int 110 :str "n"}
   :o {:int 111 :str "o"}
   :p {:int 112 :str "p"}
   :q {:int 113 :str "q"}
   :r {:int 114 :str "r"}
   :s {:int 115 :str "s"}
   :t {:int 116 :str "t"}
   :u {:int 117 :str "u"}
   :v {:int 118 :str "v"}
   :w {:int 119 :str "w"}
   :x {:int 120 :str "x"}
   :y {:int 121 :str "y"}
   :z {:int 122 :str "z"}
   :left-brace {:int 123 :str "{"}
   :pipe {:int 124 :str "|"}
   :right-brace {:int 125 :str "}"}
   :tilde {:int 126 :str "~"}
   :backspace {:int 127 :str "BACKSPACE"}})

(defn key-by-child-val
  [child-val mapping]
  (reduce
   (fn [acc [k v]]
     (assoc acc (get k child-val) v))
   {}
   (map-invert mapping)))

(def by-int
  (key-by-child-val :int canonical-mapping))

(def by-str
  (key-by-child-val :str canonical-mapping))
