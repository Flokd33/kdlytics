(ns server.positions
  (:require
    [server.marketdata :as mdata]
    [server.static :as static]
    [server.tools :as t]
    )
  )

(def positions (atom nil))
(def top10 (atom nil))
(def characteristics (atom nil))
(def strategy-exposure (atom nil))

;-------------------------------------------------------------------------------------------------------------------------------------------
;---------------------------------------------------------------GET POSITIONS---------------------------------------------------------------
;-------------------------------------------------------------------------------------------------------------------------------------------
(defn get-clean-positions []
  (let [raw-positions (t/import-txt "positions.csv")
        raw-positions-x-cash (t/chainfilter {:ticker #(not (= % "CASH"))} raw-positions)
        yahoo-snapshot-data (group-by :ticker (mdata/get-yahoo-snapshot-data (map :ticker raw-positions-x-cash)))
        fx-data-raw (mdata/get-yahoo-last-price static/list-fx)
        fx-data (->> fx-data-raw
                      (concat [{:ticker "GBpGBP=x" :fx 0.01}
                               {:ticker "GBpEUR=x" :fx (* (:fx (first (t/chainfilter {:ticker "GBPEUR=x"} fx-data-raw))) 0.01)}
                               {:ticker "EUREUR=x" :fx 1}])
                      (group-by :ticker))
        positions-with-market-data (for [p raw-positions] (merge p (first (yahoo-snapshot-data (:ticker p)))))
        positions-with-market-data-and-cash (for [pos positions-with-market-data]
                                              (if (= (:ticker pos) "CASH")
                                                (assoc pos :trailingEps 0.0, :strategy-1 "CASH", :ytdReturn 0.0, :dividendYield 0.0, :beta 1.0,
                                                  :fiftyTwoWeekLow 0.0, :profitMargins 0.0, :strategy-3 "", :lastDividendValue 0.0, :fiveYearAvgDividendYield 0.0,
                                                  :enterpriseToEbitda 0.0, :priceToBook 1, :bookValue 0.0, :marketCap 0.0, :payoutRatio 0.0, :forwardPE 0.0,
                                                  :currency (if (or (= (:account pos) "PEA") (= (:account pos) "PEAPME") (= (:account pos) "CT")) "EUR" "GBP"),
                                                  :shortName "CASH", :fiftyTwoWeekHigh 0.0, :52WeekChange 0.0, :regularMarketPrice 1, :forwardEps 0.0, :trailingPE 0.0) pos))
        positions-clean (->>  positions-with-market-data-and-cash
                              (map #(assoc % :quantity (read-string (:quantity %))))
                              (map #(assoc % :cost-per-unit (read-string (:cost-per-unit %))))
                              (map #(assoc % :nb-strategies (count (remove empty? [(:strategy-1 %) (:strategy-2 %) (:strategy-3 %)]))))

                              (map #(assoc % :cost-value-local (* (:quantity %) (:cost-per-unit %))))
                              (map #(assoc % :nav-local (* (:quantity %) (:regularMarketPrice %))))
                              (map #(assoc % :pnl-local (- (:nav-local %) (:cost-value-local %))))
                              (map #(assoc % :pnl-local-perc (/ (:pnl-local %) (:cost-value-local %))))

                              (map #(assoc % :nav-eur (* (:nav-local %) (:fx (first (fx-data (str (:currency %) "EUR=x")))) )))
                              (map #(assoc % :pnl-eur (* (:pnl-local %) (:fx (first (fx-data (str (:currency %) "EUR=x")))) )))
                              (map #(assoc % :pnl-eur-perc (* (:pnl-local-perc %) (:fx (first (fx-data (str (:currency %) "EUR=x"))))))))
        nav-eur (reduce + (map :nav-eur positions-clean))
        allocation-count (into {}
                           (for [strat (keys static/allocation-model)]
                           [strat (count (filter #(= % strat) (concat (map :strategy-3 positions-clean) (map :strategy-2 positions-clean) (map :strategy-1 positions-clean))))]))
        positions-alloc (->>  positions-clean
                              (map #(assoc % :nav-eur-perc (/ (:nav-eur %) nav-eur )))
                              (map #(assoc % :nav-eur-perc-per-strat (/ (:nav-eur-perc %) (:nb-strategies %) )))
                              (map #(assoc % :alloc-strat-1 (if (= (:strategy-1 %) "") 0 (/ (* nav-eur (/ (static/allocation-model (:strategy-1 %)) 100)) (allocation-count (:strategy-1 %))))))
                              (map #(assoc % :alloc-strat-2 (if (= (:strategy-2 %) "") 0 (/ (* nav-eur (/ (static/allocation-model (:strategy-2 %)) 100 )) (allocation-count (:strategy-2 %))))))
                              (map #(assoc % :alloc-strat-3 (if (= (:strategy-3 %) "") 0 (/ (* nav-eur (/ (static/allocation-model (:strategy-3 %)) 100 )) (allocation-count (:strategy-3 %))))))
                              (map #(assoc % :alloc-strat-total (+ (:alloc-strat-1 %) (:alloc-strat-2 %) (:alloc-strat-3 %))))
                              (map #(assoc % :alloc-strat-delta (- (:alloc-strat-total %) (:nav-eur %))))
                        )]
    positions-alloc))
;-------------------------------------------------------------------------------------------------------------------------------------------
;---------------------------------------------------------------GET ANALYTICS---------------------------------------------------------------
;-------------------------------------------------------------------------------------------------------------------------------------------
(defn get-top-10 [clean-positions] (map #(select-keys % [:nav-eur-perc :nav-eur :ticker]) (take 10 (reverse (sort-by :nav-eur-perc clean-positions)))))
(defn get-characteristics [clean-positions] [{:metric "nav-eur" :value (reduce + (map :nav-eur clean-positions))}
                                             {:metric "count-x-cash" :value (count (t/chainfilter {:strategy-1 #(not (= % "CASH"))} clean-positions))}
                                             {:metric "count-etf" :value (count (t/chainfilter {:asset-class #(= % "ETF")} clean-positions))}
                                             {:metric "top10-weight" :value (reduce + (map :nav-eur-perc (take 10 (reverse (sort-by :nav-eur-perc clean-positions)))))}
                                             {:metric "cash" :value (reduce + (map :nav-eur-perc (t/chainfilter {:strategy-1 #(= % "CASH")} clean-positions)))}
                                             {:metric "div" :value (reduce + (map #(* (:nav-eur-perc %) (if (nil? (:dividendYield %)) 0.0 (:dividendYield %))) clean-positions))}
                                             {:metric "fpe" :value (reduce + (map #(* (:nav-eur-perc %) (if (nil? (:forwardPE %)) 15 (:forwardPE %))) clean-positions))}
                                             {:metric "pb" :value (reduce + (map #(* (:nav-eur-perc %) (if (nil? (:priceToBook %)) 3 (:priceToBook %))) clean-positions))}
                                             {:metric "beta" :value (reduce + (map #(* (:nav-eur-perc %) (if (nil? (:beta %)) 1 (:beta %))) clean-positions))}])

(defn get-strategy-exposure [clean-positions]
  (for [strat (distinct (map :strategy-1 clean-positions))]
    {:strategy strat :nav-eur-perc (+ (reduce + (map :nav-eur-perc-per-strat (t/chainfilter {:strategy-1 #(= % strat)} clean-positions)))
                                      (reduce + (map :nav-eur-perc-per-strat (t/chainfilter {:strategy-2 #(= % strat)} clean-positions)))
                                      (reduce + (map :nav-eur-perc-per-strat (t/chainfilter {:strategy-3 #(= % strat)} clean-positions))))}))

;-------------------------------------------------------------------------------------------------------------------------------------------
;---------------------------------------------------------------RUN-------------------------------------------------------------------------
;-------------------------------------------------------------------------------------------------------------------------------------------

(defn run-positions! []
  (let [clean-positions (get-clean-positions)
        top10_data (get-top-10 clean-positions)
        characteristics_data (get-characteristics clean-positions)
        strategy-exposure_data (get-strategy-exposure clean-positions)]
    (reset! positions clean-positions)
    (reset! top10 top10_data)
    (reset! characteristics characteristics_data)
    (reset! strategy-exposure strategy-exposure_data)
    )
  )