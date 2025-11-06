(ns workshop.todo-test
  (:use [clojure.test]
        [com.rpl.rama]
        [com.rpl.rama.path])
  (:require
   [com.rpl.rama.test :as rtest]
   [workshop.todo :as todo]))


(defn agg-op-counts
  [data]
  (apply merge-with + data))

(deftest todo-module-test
  (with-open [ipc (rtest/create-ipc)]
    (rtest/launch-module! ipc todo/TodoAppModule {:tasks 2 :threads 2})
    (let [module-name        (get-module-name todo/TodoAppModule)
          user-depot         (foreign-depot ipc module-name "*user-depot")
          list-depot         (foreign-depot ipc module-name "*list-depot")
          profiles           (foreign-pstate ipc module-name "$$profiles")
          lists              (foreign-pstate ipc module-name "$$lists")
          list-ops-telemetry (foreign-pstate ipc module-name "$$list-ops-telemetry")
          list1              (random-uuid)
          list2              (random-uuid)
          list3              (random-uuid)
          todo1              (random-uuid)
          todo2              (random-uuid)
          todo3              (random-uuid)]
      (testing "User creation"
        (is (= {} (foreign-append! user-depot (todo/->CreateUser "alice" "Alice Smith"))))
        (is (= {"core" {:error "User already exists"}}
               (foreign-append! user-depot (todo/->CreateUser "alice" "Alice Johnson"))))
        (is (= {} (foreign-append! user-depot (todo/->CreateUser "bob" "Bob Cagney"))))
        (is (= {} (foreign-append! user-depot (todo/->CreateUser "charlie" "Charlie Davis")))))
      (is (= {:name "Alice Smith"}
             (foreign-select-one [(keypath "alice") (submap [:name])] profiles)))
      (is (foreign-select-one [(keypath "alice") :created-at-millis] profiles))

      #_
        (testing "Profile edit"
          (foreign-append! user-depot (todo/->EditProfileField "alice" :location "Honolulu"))
          (is (= {:name "Alice Smith" :location "Honolulu"}
                 (foreign-select-one [(keypath "alice") (submap [:name :location])] profiles))))

      #_
        (testing "Create list"
          (foreign-append! list-depot (todo/->CreateList list1 "alice" "List 1"))
          (is (= "List 1" (foreign-select-one [(keypath list1) :name] lists)))
          (is (= [list1] (foreign-select [(keypath "alice") :lists ALL] profiles)))
          (is (= ["alice"] (foreign-select [(keypath list1) :owners ALL] lists))))

      #_
        (testing "Multiple created lists"
          (foreign-append! list-depot (todo/->CreateList list2 "bob" "List 2"))
          (is (= [list1] (foreign-select [(keypath "alice") :lists ALL] profiles)))
          (is (= ["alice"] (foreign-select [(keypath list1) :owners ALL] lists)))
          (is (= [list2] (foreign-select [(keypath "bob") :lists ALL] profiles)))
          (is (= ["bob"] (foreign-select [(keypath list2) :owners ALL] lists))))

      #_
        (testing "Multiple lists owned by one user"
          (foreign-append! list-depot (todo/->CreateList list3 "alice" "List 3"))
          (is (= #{list1 list3} (set (foreign-select [(keypath "alice") :lists ALL] profiles))))
          (is (= ["alice"] (foreign-select [(keypath list3) :owners ALL] lists))))

      #_
        (testing "Shared list"
          (foreign-append! user-depot (todo/->ShareList "alice" list1 "charlie"))
          (is (= [list1] (foreign-select [(keypath "charlie") :lists ALL] profiles)))
          (is (= #{"alice" "charlie"} (set (foreign-select [(keypath list1) :owners ALL] lists))))

          (foreign-append! user-depot (todo/->ShareList "alice" list1 "bob"))
          (is (= #{list1 list2} (set (foreign-select [(keypath "bob") :lists ALL] profiles))))
          (is (= #{"alice" "bob" "charlie"}
                 (set (foreign-select [(keypath list1) :owners ALL] lists)))))

      #_
        (testing "Share unowned list"
          (foreign-append! user-depot (todo/->ShareList "alice" list2 "charlie"))
          (is (= #{list1 list3} (set (foreign-select [(keypath "alice") :lists ALL] profiles))))
          (is (= [list1] (foreign-select [(keypath "charlie") :lists ALL] profiles)))
          (is (= ["bob"] (foreign-select [(keypath list2) :owners ALL] lists))))

      #_
        (testing "Add todos"
          (foreign-append! list-depot (todo/->AddTodo list1 todo1 "abc"))
          (foreign-append! list-depot (todo/->AddTodo list1 todo2 "def"))
          (foreign-append! list-depot (todo/->AddTodo list1 todo3 "ghi"))
          (is (= [(todo/->TodoItem todo1 "abc" false)
                  (todo/->TodoItem todo2 "def" false)
                  (todo/->TodoItem todo3 "ghi" false)]
                 (foreign-select [(keypath list1) :items ALL] lists))))

      #_
        (testing "Edit todo"
          (foreign-append! list-depot (todo/->EditTodo list1 todo2 :complete? true))
          (foreign-append! list-depot (todo/->EditTodo list1 todo1 :content "ABC"))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo2 "def" true)
                  (todo/->TodoItem todo3 "ghi" false)]
                 (foreign-select [(keypath list1) :items ALL] lists))))

      #_
        (testing "Move todo"
          (foreign-append! list-depot (todo/->MoveTodo list1 todo2 0))
          (is (= [(todo/->TodoItem todo2 "def" true)
                  (todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo3 "ghi" false)]
                 (foreign-select [(keypath list1) :items ALL] lists)))
          (foreign-append! list-depot (todo/->MoveTodo list1 todo2 2))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo3 "ghi" false)
                  (todo/->TodoItem todo2 "def" true)]
                 (foreign-select [(keypath list1) :items ALL] lists)))
          (foreign-append! list-depot (todo/->MoveTodo list1 todo2 2))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo3 "ghi" false)
                  (todo/->TodoItem todo2 "def" true)]
                 (foreign-select [(keypath list1) :items ALL] lists)))
          (foreign-append! list-depot (todo/->MoveTodo list1 todo2 3))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo3 "ghi" false)
                  (todo/->TodoItem todo2 "def" true)]
                 (foreign-select [(keypath list1) :items ALL] lists))))

      #_
        (testing "Delete todo"
          (foreign-append! list-depot (todo/->DeleteTodo list1 todo3))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo2 "def" true)]
                 (foreign-select [(keypath list1) :items ALL] lists))))

      #_
        (testing "Remove list"
          (foreign-append! list-depot (todo/->RemoveList list1 "alice"))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo2 "def" true)]
                 (foreign-select [(keypath list1) :items ALL] lists)))
          (is (= #{list3} (set (foreign-select [(keypath "alice") :lists ALL] profiles))))
          (is (= #{"bob" "charlie"}
                 (set (foreign-select [(keypath list1) :owners ALL] lists))))

          (foreign-append! list-depot (todo/->RemoveList list1 "charlie"))
          (is (= [(todo/->TodoItem todo1 "ABC" false)
                  (todo/->TodoItem todo2 "def" true)]
                 (foreign-select [(keypath list1) :items ALL] lists)))
          (is (= [] (foreign-select [(keypath "charlie") :lists ALL] profiles)))
          (is (= ["bob"]
                 (foreign-select [(keypath list1) :owners ALL] lists)))

          (foreign-append! list-depot (todo/->RemoveList list1 "bob"))
          (is (nil? (foreign-select-one (keypath list1) lists)))
          (is (= [list2] (foreign-select [(keypath "bob") :lists ALL] profiles))))


      #_
        (testing "Telemetry test"
          (rtest/wait-for-microbatch-processed-count ipc module-name "analytics" 16)
          (is (= {workshop.todo.CreateList 3
                  workshop.todo.AddTodo    3
                  workshop.todo.EditTodo   2
                  workshop.todo.MoveTodo   4
                  workshop.todo.DeleteTodo 1
                  workshop.todo.RemoveList 3}
                 (agg-op-counts (foreign-select MAP-VALS list-ops-telemetry)))))
    )))
