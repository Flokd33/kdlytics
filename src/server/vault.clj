(ns server.vault
  (:require
    [server.tools :as t])
  )

(def vault-summary (atom nil))

;TODO do we create a PNL/transaction summary AND a inventory ? I think Yes
(defn get-vault-summary []
  (let [raw-data (t/import-txt "vault.csv")
        clean-data (->>  raw-data
                         (map #(assoc % :quantity (read-string (:quantity %))))
                         (map #(assoc % :price (read-string (:price %))))
                         (map #(assoc % :spot_usd (read-string (:spot_usd %))))
                         ;add item_characteristics (from catalogue flat file ?)
                         ;add premium
                         ;add current market value
                         ;add pnl
                         )]
    clean-data))


(defn refresh-vault-data! []
  (reset! vault-summary (get-vault-summary))
  )