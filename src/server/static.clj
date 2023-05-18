(ns server.static)


;(def list-tickers ["RIO.L" "BP.L" "GLEN.L" "AAL.L" "LGEN.L" "MC.PA" "TTE.PA" "ORA.PA" "RUI.PA" "STMPA.PA"])
(def list-fx ["GBPEUR=x" "USDEUR=x"])
(def list-metals ["GC=F" "SI=F" "PL=F" "PA=F"])
(def list-field-snapshot ["shortName" "currency" "regularMarketPrice" "52WeekChange" "ytdReturn" "marketCap" "fiftyTwoWeekHigh" "fiftyTwoWeekLow"
                          "beta" "priceToBook" "forwardPE" "trailingPE" "enterpriseToEbitda" "payoutRatio" "dividendYield"  "fiveYearAvgDividendYield" "lastDividendValue" ;
                           "trailingEps" "forwardEps" "bookValue" "profitMargins"])
(def allocation-model {"IQ" 40
                       "CACXSTATE" 20
                       "M&M" 10
                       "EM" 10
                       "SEMICONDUCTORS" 10
                       "DRONE" 5
                       "CASH" 5
                       })
