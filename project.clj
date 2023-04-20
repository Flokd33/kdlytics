(defproject flo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]

                 [clj-http "3.12.3"]                        ; for http request, use slurp for simple get, this for post request
                 [cheshire "5.11.0"]                        ; for JSON read

                 ]
  :repl-options {:init-ns flo.core})

