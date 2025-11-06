(ns workshop.stream
  (:use [com.rpl.rama]
        [com.rpl.rama path])
  (:require
    [com.rpl.rama.test :as rtest]
    [workshop.ipc :as ipc]))

(defrecord Item [k1 k2])

(defmodule StreamModule
  [setup topologies]
  (declare-depot setup *depot :random)

  (let [s (stream-topology topologies "core")]
    (declare-pstate s $$counts {String Long})
    (<<sources s
      (source> *depot :> {:keys [*k1 *k2]})
      ;; TODO
      (|hash *k1)
      (local-transform> [(keypath *k1) (nil->val 0) (term inc)] $$counts)
      (local-select> (keypath *k1) $$counts :> *k1-count)
      (ack-return> *k1-count)
      (|hash *k2)
      ;; TODO
      (local-transform> [(keypath *k2) (nil->val 0) (term inc)] $$counts)
      )))

(defn launch! []
  (ipc/reset-ipc!)
  (rtest/launch-module! @ipc/IPC StreamModule {:tasks 16 :threads 2})
  (let [module-name (get-module-name StreamModule)]
    {:depot (foreign-depot @ipc/IPC module-name "*depot")
     :counts (foreign-pstate @ipc/IPC module-name "$$counts")
     }))
