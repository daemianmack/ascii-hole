(ns ascii-hole.core-test
  (:require [ascii-hole.core :refer :all]
            [clojure.test :refer :all]))

(deftest keyword->char   (is (= \c  (->char :c))))
(deftest string->char    (is (= \c  (->char "c"))))
(deftest int->char       (is (= \c  (->char 99))))
(deftest char->char      (is (= \c  (->char \c))))
(deftest ctrl-char->char (is (= \ (->char \))))

(deftest char-keys-works
  (is (= {\c :c \d :d} (char-keys {:c :c :d :d}))))
