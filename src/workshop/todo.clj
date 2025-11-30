(ns workshop.todo
  (:use [com.rpl.rama]
        [com.rpl.rama path])
  (:require
   [com.rpl.rama.aggs :as aggs]
   [com.rpl.rama.test :as rtest]
   [workshop.ipc :as ipc])
  (:import
   [java.util
    UUID]))


;; for *user-depot
(defrecord CreateUser
  [username name])

(defrecord EditProfileField
  [username key value])

(defrecord ShareList
  [username list-id to-username])

;; for *list-depot
(defrecord CreateList
  [list-id username name])

(defrecord RemoveList
  [list-id username])

(defrecord AddTodo
  [list-id todo-id content])

(defrecord DeleteTodo
  [list-id todo-id])

(defrecord EditTodo
  [list-id todo-id key value])

(defrecord MoveTodo
  [list-id todo-id to-index])

;; for $$lists
(defrecord TodoItem
  [todo-id content complete?])


(defn create-interactive-topology!
  [topologies]
  (let [s (stream-topology topologies "core")]
    (declare-pstate
     s
     $$profiles
     {String
      (fixed-keys-schema
       {:name     String
        :location String
        :created-at-millis Long
        :lists    (set-schema UUID {:subindex? true})
       })})
    (declare-pstate
     s
     $$lists
     {UUID
      (fixed-keys-schema
       {:name   String
        :created-at-millis Long
        :items  [TodoItem]
        :owners (set-schema String {:subindex? true})
       })})

    (<<sources s
     (source> *user-depot :> {:keys [*username] :as *data})
      (<<subsource *data
       (case> CreateUser :> {:keys [*name]})
      ;; TODO

       (case> EditProfileField :> {:keys [*key *value]})
        ;; TODO

       (case> ShareList :> {:keys [*list-id *to-username]})
        ;; TODO
      )

     (source> *list-depot :> {:keys [*list-id] :as *data})
      (<<subsource *data
       (case> CreateList :> {:keys [*username *name]})
        ;; TODO

       (case> RemoveList :> {:keys [*username]})
      ;: TODO

       (case> AddTodo :> {:keys [*content]})
      ;; TODO

       (case> EditTodo :> {:keys [*todo-id *key *value]})
      ;; TODO

       (case> DeleteTodo :> {:keys [*todo-id]})
      ;; TODO

       (case> MoveTodo :> {:keys [*todo-id *to-index]})
        ;; TODO
      )
    )))

(defn current-minute-bucket
  []
  (-> (System/currentTimeMillis)
      (/ 1000)
      (/ 60)
      long))

(defn create-analytics-topology!
  [topologies]
  (let [mb (microbatch-topology topologies "analytics")]
    (declare-pstate mb
                    $$list-ops-telemetry
                    {Long ; minute bucket
                     (map-schema
                      Class ; operation type
                      Long ; count
                     )})

    (<<sources mb
     (source> *list-depot :> %mb)
      ;; TODO
    )
  ))

(defmodule TodoAppModule
  [setup topologies]
  (declare-depot setup *user-depot (hash-by :username))
  (declare-depot setup *list-depot (hash-by :list-id))

  (create-interactive-topology! topologies)
  (create-analytics-topology! topologies))


(defn launch! []
  (ipc/reset-ipc!)
  (rtest/launch-module! @ipc/IPC TodoAppModule {:tasks 4 :threads 2})
  (let [module-name (get-module-name TodoAppModule)]
    {:user-depot (foreign-depot @ipc/IPC module-name "*user-depot")
     :list-depot (foreign-depot @ipc/IPC module-name "*list-depot")
     :profiles (foreign-pstate @ipc/IPC module-name "$$profiles")
     :lists (foreign-pstate @ipc/IPC module-name "$$lists")
     :list-ops-telemetry (foreign-pstate @ipc/IPC module-name "$$list-ops-telemetry")
     }))
