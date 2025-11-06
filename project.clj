(defproject com.rpl/rama-workshop "1.0.0-SNAPSHOT"
  :jvm-opts ["-Xss6m"
             "-Xms6g"
             "-Xmx6g"]
  :dependencies [[com.rpl/rama-helpers "0.10.0" :exclusions [org.clojure/clojure]]]
  :global-vars {*warn-on-reflection* true}
  :repositories
  [["releases"
    {:id  "maven-releases"
     :url "https://nexus.redplanetlabs.com/repository/maven-public-releases"}]]
  :profiles {:dev      {:resource-paths ["test/resources/"]}
             :provided {:dependencies [[com.rpl/rama "1.1.0"]
                                       [org.clojure/clojure "1.12.0"]]}}
)
