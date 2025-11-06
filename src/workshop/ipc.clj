(ns workshop.ipc
  (:use [com.rpl.rama]
        [com.rpl.rama path])
  (:require [com.rpl.rama.test :as rtest]))

(def IPC (volatile! nil))

(defn reset-ipc! []
  (when-let [ipc @IPC]
    (close! ipc)
    (vreset! IPC nil))
  (vreset! IPC (rtest/create-ipc)))
