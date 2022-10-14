(defproject clj-scrapper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0",
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [enlive "1.1.6"]
                 [org.clojure/data.json "2.4.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all,
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]},
             :classes-by-plan {:main clj-scrapper.classes-by-plan},
             :classes-by-college {:main clj-scrapper.classes-by-college}}
  :aliases {"classes-by-plan" ["with-profile" "classes-by-plan" "run"],
            "classes-by-college" ["with-profile" "classes-by-college" "run"]})
