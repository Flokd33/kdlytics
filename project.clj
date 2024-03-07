(defproject kdlytics "0.1.0-SNAPSHOT"
  :description "Wealth Management Monitoring Tool"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 ;[org.clojure/tools.logging "1.2.4"]        ; for logs
                 [clj-http "3.12.3"]                        ; for http request, use slurp for simple get, this for post request
                 [metosin/jsonista "0.3.7"]

                 ;CSV/JSON + TRANSIT
                 [com.cnuernber/charred "1.028"]
                 [cheshire "5.11.0"]                                ; for JSON read


                 [metosin/jsonista "0.3.7"]                               ;for transit
                 [com.fasterxml.jackson.core/jackson-core "2.14.0"]       ;necessary for jsonista
                 [com.fasterxml.jackson.core/jackson-databind "2.14.0"]    ;necessary for jsonista
                 [com.fasterxml.jackson.datatype/jackson-datatype-jsr310 "2.14.0"] ;necessary for jsonista


                 ;RING / HTTP / ETC
                 [com.cognitect/transit-clj "1.0.329"]      ;for transit ?

                 [ring-logger "1.1.1"]                      ;for server setup
                 [amalloy/ring-gzip-middleware "0.1.4"]     ;for server setup
                 [compojure "1.6.3"]                        ;for server setup
                 [ring/ring-defaults "0.3.4"]               ;for server setup
                 [ring-cors "0.1.13"]                       ;for server setup
                 [http-kit "2.6.0"]                         ;webscraping + server
                 [enlive "1.1.6"]                           ;webscraping
                 ]
  :repl-options {:init-ns server.core}
  ;THIS IS NEEDED AT LEAST FROM WORK TO TAKE WINDOWS SSL CERTIFICATE
  :jvm-opts ["-Xmx24g"
             "--add-opens=java.base/java.nio=ALL-UNNAMED"
             "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
             "-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT"
             "-Dorg.bytedeco.openblas.load=mkl_rt"
             "--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED"]
  )
