(ns workshop.pstate
  (:use [com.rpl.rama]
        [com.rpl.rama path])
  (:require
    [com.rpl.rama.test :as rtest]))


(defn test-pstate
  []
  (rtest/create-test-pstate {String {String [Object]}}))
