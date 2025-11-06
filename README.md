## Deploying module onto local cluster

```
./rama devZookeeper &
./rama conductor &
./rama supervisor &

./rama deploy \
  --action launch \
  --systemModule monitoring \
  --tasks 1 \
  --threads 1 \
  --workers 1

./rama deploy \
  --action launch \
  --jar <path to uberjar> \
  --module workshop.todo/TodoAppModule \
  --tasks 4 \
  --threads 2 \
  --workers 1

./rama repl --jar <path to uberjar>


(use 'com.rpl.rama)
(use 'com.rpl.rama.path)
(require '[workshop.todo :as todo])
(def cluster (open-cluster-manager {"conductor.host" "localhost"}))
(def user-depot (foreign-depot cluster "workshop.todo/TodoAppModule" "*user-depot"))
(def profiles (foreign-pstate cluster "workshop.todo/TodoAppModule" "$$profiles"))


(foreign-append! user-depot (todo/->CreateUser "alice" "Alice Smith"))
(foreign-select-one [(keypath "alice") (submap [:name :location :created-at-millis])] profiles)
```
