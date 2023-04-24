(defproject kdlytics "0.1.0-SNAPSHOT"
  :description "Wealth Management Monitoring Tool"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]

                 [clj-http "3.12.3"]                        ; for http request, use slurp for simple get, this for post request
                 [cheshire "5.11.0"]                        ; for JSON read

                 [http-kit "2.1.18"]                         ;webscraping NOOO NEED
                 [enlive "1.1.6"]                           ;webscraping
                 ]
  :repl-options {:init-ns server.core})
