(ns server.core
  (:require
    [server.api :as api]))

(defn -main []
  (println "Let's go!")
  (println "Starting API server on port 3501...")
  (api/start-web-server! 3501)
  (api/daily-run!)
  ;(log/info "Setting up schedules")
  ;(let [schedules [{:fn tadb/daily-run!                   :start (ms-to-time 2 0 0)}]] (doseq [line schedules] (.scheduleAtFixedRate daily-scheduler-pool (try-catch-throw #((:fn line))) (:start line) (* 24 3600 1000) TimeUnit/MILLISECONDS)))
  (println "Ready!"))