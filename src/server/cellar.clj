(ns server.cellar
  (:require
    [server.tools :as t]))

(def cellar-summary (atom nil))

(defn get-cellar-summary []
  (let [raw-data (t/import-txt "cellar.csv")
        clean-data (->>  raw-data
                         (map #(assoc % :quantity (read-string (:quantity %))))
                         (map #(assoc % :cost_per_unit (read-string (:cost_per_unit %))))
                         (map #(assoc % :market_price (read-string (:market_price %))))
                         )]
    clean-data))


(defn refresh-cellar-data! []
  (reset! cellar-summary (get-cellar-summary))
  )