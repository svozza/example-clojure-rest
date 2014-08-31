(defproject example-clojure-rest "0.1.0-SNAPSHOT"
      :description "Example REST service"
      :url "https://example-clojure-rest-c9-svozza.c9.io"
      :dependencies [[org.clojure/clojure "1.4.0"]
                     [ring/ring-core "1.3.0"]
                     [compojure "1.1.1" :exclusions [ring/ring-core]]
                     [clojurewerkz/scrypt "1.2.0"]
                     [ring/ring-json "0.2.0"]
                     [ring-middleware-format "0.4.0"]
                     [java-jdbc/dsl "0.1.0"]
                     [org.clojure/java.jdbc "0.3.0"]
                     [mysql/mysql-connector-java "5.1.32"]
                     ;[org.slf4j/slf4j-log4j12 "1.5.0"]
                     [org.slf4j/slf4j-nop "1.5.0"]
                     [com.jolbox/bonecp "0.7.1.RELEASE"]
                     [cheshire "4.0.3"]]
      :plugins [[lein-ring "0.7.3"]]
      :ring {:handler example-clojure-rest.handler/app}
      :profiles
      {:dev {:dependencies [[ring-mock "0.1.3"]]}})
