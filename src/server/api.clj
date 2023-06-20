(ns server.api
  (:require
    [clojure.tools.logging :as log]
    [server.marketdata :as marketdata]
    [server.positions :as positions]
    [server.bank :as bank]
    [server.vault :as vault]
    [server.cellar :as cellar])
  )


(defn daily-run! []
(try
  (log/info "Refreshing fx...")             (marketdata/refresh-fx-data!)
  (log/info "Refreshing commodities...")    (marketdata/refresh-commodities-data!)
  (log/info "Refreshing positions...")      (positions/refresh-positions-data!)
  (log/info "Refreshing bank...")           (bank/refresh-bank-data!)
  (log/info "Refreshing vault...")          (vault/refresh-vault-data!)
  (log/info "Refreshing cellar...")         (cellar/refresh-cellar-data!)))

;TODO create calls to server