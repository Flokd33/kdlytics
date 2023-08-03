(ns server.marketdata
  (:require
    [server.static :as static]
    [cheshire.core :as cheshire]
    )
  )
;(:import java.time.LocalDate)
;(LocalDate/parse "2019-01-01")

;----------------------------------------------EXTRACT FROM YAHOO FINANCE-----------------------------------
;certainly the best and easiest way to get prices and basic stats but not for fundamental/valuation metrics
;https://github.com/dakrone/clj-http
;(slurp) will return the html content of a http.. basically the body of the http/get answer
;Yahoo Finance API seems to be deprecated, V10 out of the game, V7 as well, V6 still here but need to query module by module... we need alternative solution
;https://www.reddit.com/r/GnuCash/comments/1385t2m/looks_like_yahoo_json_just_broke/
;https://www.reddit.com/r/sheets/comments/14yjyqg/yfinance_yahoo_link_does_not_work/

(def query-head-snapshot "https://query2.finance.yahoo.com/v6/finance/quoteSummary/") ;v10 stopped working, V6 works but cannot request all modules together
(def query-tail-snapshot "?modules=defaultKeyStatistics%2CsummaryDetail%2CsummaryDetail%2Cprice&ssl=true") ;this was for v10, where we were requesting multiple modules at once
(def query-tail-price "?modules=price")

(defn get-yahoo-last-price [list-ticker]                    ;price non adjusted
  "Extract latest price from yahoo finance - used for FX and metals"
  (let [fx-data (flatten
                  (for [ticker list-ticker]
                    (let [results (get (first (vals (first (get-in (cheshire.core/parse-string (slurp (str query-head-snapshot ticker query-tail-price))) ["quoteSummary" "result"])))) "regularMarketPrice")]
                      (into {}
                            {:ticker ticker :value (get results "raw")}))))
        ]
    fx-data
    )
  )

(defn get-yahoo-snapshot-data [list-tickers]
  "Extract snapshot data from yahoo finance for a list of tickers...static data, key stats and last price"
  (let [snapshot-data-raw (flatten
                            (for [ticker list-tickers]
                              (let [raw-result (get-in (cheshire.core/parse-string (slurp (str query-head-snapshot ticker query-tail-snapshot))) ["quoteSummary" "result"])
                                    result-clean (into {} (flatten (for [module raw-result] (vals module))))]
                                (for [field static/list-field-snapshot]
                                  (let [field-value (get result-clean field)]
                                    (into {}
                                          {:ticker ticker
                                           (keyword field) (if (or (= field "shortName") (= field "currency"))
                                                             field-value
                                                             (get field-value "raw"))}))))))


        snapshot-data-clean (let [data-grouped (group-by :ticker snapshot-data-raw)]
                              (for [ticker list-tickers]
                                (assoc (into {} (map #(second %) (data-grouped ticker))) :ticker ticker)))
        ]
    snapshot-data-clean
    )
  )


(defn get-yahoo-price-history [ticker period start-date]    ;use close not adj close...
  "Get historical price data from Yahoo finance for a given ticker - period is one of 1m, 5m, 15m, 30m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo - start-date format is YYYY-MM-DD "
  (let [list-field ["date" "open" "high" "low" "close" "adj-close" "volume"]
        start-date-unix (int (/ (.getTime (read-string (str "#inst \"" start-date "\"" ))) 1000)) ;(.getTime #inst"1988-07-04T00:00:00.000-00:00")
        end-date-unix (int (/ (.getTime (java.util.Date.)) 1000))
        query-head "https://query1.finance.yahoo.com/v7/finance/download/"
        query-tail (str "?period1=" start-date-unix "&period2=" end-date-unix "&interval=" period "&events=history")
        raw-results (slurp (str query-head ticker query-tail))
        clean-results (for [line (clojure.string/split-lines raw-results)] (for [word (clojure.string/split line #",")] (clojure.string/trim word)))
        historical-price-data (map #(zipmap (map keyword list-field) %) (rest clean-results)) ;instead of list field we can use keyword of first results with fct below  ;;;;;;;;;;; (clojure.string/join "" (filter #(not (clojure.string/blank? %)) (clojure.string/split " AAAA TTT " #" "))) ;;;;;;;;;;
        ]
    historical-price-data))

(defn get-fred-macro-data []
  "Get economic data from FRED API"
  )


;--------------------------------------------DATA FROM ALPHAVANTAGE-------------------------
;use for overview and last price aj = unadj

(def key-test "ZMGQ4U17GVTUOR7V")
(def ticker "TTE.PA")
(def data-type "json")                                      ;or csv
(def endpoint (str "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" ticker "&apikey=" key-test))
(def endpoint-p (str "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" ticker "&apikey=" key-test "&datatype=" data-type  )) ;adjusted price requires PREMIUM...

;(get (first (vals (first (get-in (cheshire.core/parse-string (slurp (str query-head-snapshot ticker query-tail-price))) ["quoteSummary" "result"])))) "regularMarketPrice")

;------------------------------------------------FX/COMMODITIES REFRESH-----------------------------------
(def fx-data (atom nil))
(def commodities-data (atom nil))

(defn refresh-fx-data! [] (reset! fx-data (get-yahoo-last-price static/list-fx)))
(defn refresh-commodities-data! [] (reset! commodities-data (get-yahoo-last-price static/list-commodities)))







