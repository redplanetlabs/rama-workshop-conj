(ns workshop.depot
  (:use [com.rpl.rama]
        [com.rpl.rama path])
  (:require
    [com.rpl.rama.test :as rtest]
    [workshop.ipc :as ipc]))

(defmodule DepotModule
  [setup topologies]
  (declare-depot setup *depot :random))

(defn launch! []
  (ipc/reset-ipc!)
  (rtest/launch-module! @ipc/IPC DepotModule {:tasks 4 :threads 2})
  (let [module-name (get-module-name DepotModule)]
    {:depot (foreign-depot @ipc/IPC module-name "*depot")
     }))

(defmodule MultiDepotModule
  [setup topologies]
  ;; TODO add two depots:
  ;;  - *hash-depot, with hash partitioning on key :id
  ;;  - *global-depot, declared as global
  )



(defn launch-multi! []
  (ipc/reset-ipc!)
  (rtest/launch-module! @ipc/IPC MultiDepotModule {:tasks 4 :threads 2})
  (let [module-name (get-module-name MultiDepotModule)]
    {:hash-depot (foreign-depot @ipc/IPC module-name "*hash-depot")
     :global-depot (foreign-depot @ipc/IPC module-name "*global-depot")
     }))

(comment
  (use 'com.rpl.rama)
  (require '[workshop.depot :as depot] :reload)
  (def depot (:depot (depot/launch!)))

  (dotimes [i 10]
    (foreign-append! depot {:a i}))

  (foreign-object-info depot)

  (foreign-depot-partition-info depot 0)

  (foreign-depot-read depot 0 0 10)
  )
