(defproject lake "0.1.0-SNAPSHOT"
  :description "Tower's data lake"
  :url "https://github.com/piperswe/lake"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.postgresql/postgresql "42.2.8.jre7"]
                 [manifold "0.1.8"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 ; lake.rpc
                 [com.cognitect/transit-clj "0.8.319"]
                 [clj-html-compressor "0.1.1"]
                 [potemkin "0.4.5"]
                 ; lake.mq
                 [overtone/at-at "1.2.0"]
                 ; lake.archive
                 [cheshire "5.9.0"]
                 [com.novemberain/pantomime "2.11.0"]
                 [hickory "0.7.1"]
                 [clojurewerkz/urly "1.0.0"]
                 ; lake.web
                 [ring "1.8.0"]
                 [compojure "1.6.1"]
                 ; lake.email
                 [io.forward/clojure-mail "1.0.8"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler lake.web.server/app
         :nrepl {:start? true}}
  :deploy-repositories {"github" {:url      "https://maven.pkg.github.com/piperswe"
                                  :username "piperswe"
                                  :password :env/github_token}}
  :repl-options {:init-ns lake.core}
  :main lake.core
  :profiles {:uberjar {:aot :all}
             :test    {:dependencies [[mockery "0.1.4"]]}})
