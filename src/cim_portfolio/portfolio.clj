;; gorilla-repl.fileformat = 1

;; **
;;; # CIM Portfolio
;; **

;; **
;;; ### Required packages
;; **

;; @@
(ns cim_portfolio.portfolio
  (:require [cim_portfolio.util :as util]
            [cim_portfolio.yfinanceclient :as client]
            [cim_portfolio.plot :as plot]
            [clojure.math :as math]
            ))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Portfolio Analysis Helper Functions
;; **

;; @@
;; Computes arithmetic, log, and cumulative return for a given list of prices
(defn calculate-returns [prices]
  (let [price-changes (map #(double (/ (second %) (first %))) (partition 2 1 prices))
        arithmetic-returns (mapv #(- % 1.0) price-changes)
        log-returns (mapv #(Math/log %) price-changes)
        cumulative-log-return (reduce + log-returns)]
    {
     :cumulative-return cumulative-log-return
     :arithmetic-returns arithmetic-returns
     :log-returns log-returns
     }))

;; Variation of the above function - additionally associates the returns with their corresponding date
(defn calculate-returns-with-corresponding-date [prices dates]
  (let [price-changes (map #(double (/ (second %) (first %))) (partition 2 1 prices))
        arithmetic-returns (mapv #(- % 1.0) price-changes)
        log-returns (mapv #(Math/log %) price-changes)
        cumulative-log-return (reduce + log-returns)]
    {
     :cumulative-return cumulative-log-return
     :arithmetic-returns (util/sort-map-by-date (zipmap (rest dates) arithmetic-returns))
     :log-returns (util/sort-map-by-date (zipmap (rest dates) log-returns))
     }))

;; Calculates portfolio return - accepts map {:stock cash-invested} and stock-performance generated by calculate-returns
(defn calculate-portfolio-return [cash-invested returns]
  (let [total-investment (apply + (vals cash-invested))]
    (->> cash-invested
         (map (fn [[security invested]]
                (let [weight (/ invested total-investment)
                      return (:cumulative-return (get returns security))]
                  (* weight return))))
         (apply +))))

;; Calculates cumulative return UP TILL a given date
(defn get-cumulative-return-till-given-date [cumulative-returns date]
  (util/sum-up-to-key date cumulative-returns)
)

;; Only calculates portfolio return up till a given date
(defn calculate-portfolio-return-for-given-date [cash-invested returns date]
  (let [total-investment (apply + (vals cash-invested))]
    (->> cash-invested
         (map (fn [[security invested]]
                (let [weight (/ invested total-investment)
                      return (get-cumulative-return-till-given-date (:log-returns (get returns security)) date)
                     ]
                  (* weight return))))
         (apply +))))

()

; Calculates the annualized return of the portfolio (accepts the starting value, ending value of portfolio, the start and end date)
(defn calculate-annualized-return [starting-value ending-value start-date end-date]
  (let [return (/ (- ending-value starting-value) starting-value)
        number-of-days (util/number-of-days-between start-date end-date)
       ]
    (- (math/pow (+ 1 return) (/ 365 number-of-days)) 1)
  )
)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cim_portfolio.portfolio/calculate-annualized-return</span>","value":"#'cim_portfolio.portfolio/calculate-annualized-return"}
;; <=

;; @@
(calculate-returns [100 110 120 130])
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:cumulative-return</span>","value":":cumulative-return"},{"type":"html","content":"<span class='clj-double'>0.262364264467491</span>","value":"0.262364264467491"}],"value":"[:cumulative-return 0.262364264467491]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:arithmetic-returns</span>","value":":arithmetic-returns"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.10000000000000009</span>","value":"0.10000000000000009"},{"type":"html","content":"<span class='clj-double'>0.09090909090909105</span>","value":"0.09090909090909105"},{"type":"html","content":"<span class='clj-double'>0.08333333333333304</span>","value":"0.08333333333333304"}],"value":"[0.10000000000000009 0.09090909090909105 0.08333333333333304]"}],"value":"[:arithmetic-returns [0.10000000000000009 0.09090909090909105 0.08333333333333304]]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:log-returns</span>","value":":log-returns"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.09531017980432493</span>","value":"0.09531017980432493"},{"type":"html","content":"<span class='clj-double'>0.0870113769896299</span>","value":"0.0870113769896299"},{"type":"html","content":"<span class='clj-double'>0.08004270767353615</span>","value":"0.08004270767353615"}],"value":"[0.09531017980432493 0.0870113769896299 0.08004270767353615]"}],"value":"[:log-returns [0.09531017980432493 0.0870113769896299 0.08004270767353615]]"}],"value":"{:cumulative-return 0.262364264467491, :arithmetic-returns [0.10000000000000009 0.09090909090909105 0.08333333333333304], :log-returns [0.09531017980432493 0.0870113769896299 0.08004270767353615]}"}
;; <=

;; @@
(calculate-annualized-return 2000 2500 "2023-10-16" "2024-04-27")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.5217057952294997</span>","value":"0.5217057952294997"}
;; <=

;; **
;;; ### YFinance API
;; **

;; @@
(client/get-ticker-price-all "NVDA" "2024-04-15")
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-16&quot;</span>","value":"\"2024-04-16\""},{"type":"html","content":"<span class='clj-double'>864.3300170898</span>","value":"864.3300170898"},{"type":"html","content":"<span class='clj-double'>874.1500244141</span>","value":"874.1500244141"}],"value":"[\"2024-04-16\" 864.3300170898 874.1500244141]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-17&quot;</span>","value":"\"2024-04-17\""},{"type":"html","content":"<span class='clj-double'>883.4000244141</span>","value":"883.4000244141"},{"type":"html","content":"<span class='clj-double'>840.3499755859</span>","value":"840.3499755859"}],"value":"[\"2024-04-17\" 883.4000244141 840.3499755859]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-18&quot;</span>","value":"\"2024-04-18\""},{"type":"html","content":"<span class='clj-double'>849.700012207</span>","value":"849.700012207"},{"type":"html","content":"<span class='clj-double'>846.7100219727</span>","value":"846.7100219727"}],"value":"[\"2024-04-18\" 849.700012207 846.7100219727]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-19&quot;</span>","value":"\"2024-04-19\""},{"type":"html","content":"<span class='clj-double'>831.5</span>","value":"831.5"},{"type":"html","content":"<span class='clj-double'>762.0</span>","value":"762.0"}],"value":"[\"2024-04-19\" 831.5 762.0]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-22&quot;</span>","value":"\"2024-04-22\""},{"type":"html","content":"<span class='clj-double'>781.0399780273</span>","value":"781.0399780273"},{"type":"html","content":"<span class='clj-double'>795.1799926758</span>","value":"795.1799926758"}],"value":"[\"2024-04-22\" 781.0399780273 795.1799926758]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-23&quot;</span>","value":"\"2024-04-23\""},{"type":"html","content":"<span class='clj-double'>807.6900024414</span>","value":"807.6900024414"},{"type":"html","content":"<span class='clj-double'>824.2299804688</span>","value":"824.2299804688"}],"value":"[\"2024-04-23\" 807.6900024414 824.2299804688]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-24&quot;</span>","value":"\"2024-04-24\""},{"type":"html","content":"<span class='clj-double'>839.5</span>","value":"839.5"},{"type":"html","content":"<span class='clj-double'>796.7700195312</span>","value":"796.7700195312"}],"value":"[\"2024-04-24\" 839.5 796.7700195312]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-25&quot;</span>","value":"\"2024-04-25\""},{"type":"html","content":"<span class='clj-double'>788.6799926758</span>","value":"788.6799926758"},{"type":"html","content":"<span class='clj-double'>826.3200073242</span>","value":"826.3200073242"}],"value":"[\"2024-04-25\" 788.6799926758 826.3200073242]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;2024-04-26&quot;</span>","value":"\"2024-04-26\""},{"type":"html","content":"<span class='clj-double'>838.1799926758</span>","value":"838.1799926758"},{"type":"html","content":"<span class='clj-double'>877.3499755859</span>","value":"877.3499755859"}],"value":"[\"2024-04-26\" 838.1799926758 877.3499755859]"}],"value":"[[\"2024-04-16\" 864.3300170898 874.1500244141] [\"2024-04-17\" 883.4000244141 840.3499755859] [\"2024-04-18\" 849.700012207 846.7100219727] [\"2024-04-19\" 831.5 762.0] [\"2024-04-22\" 781.0399780273 795.1799926758] [\"2024-04-23\" 807.6900024414 824.2299804688] [\"2024-04-24\" 839.5 796.7700195312] [\"2024-04-25\" 788.6799926758 826.3200073242] [\"2024-04-26\" 838.1799926758 877.3499755859]]"}
;; <=

;; **
;;; ### Portfolio Processing Section
;; **

;; @@
(defn analyze-portfolio [data]
  (loop [cash 0.0
         portfolio {}
         portfolio-value {}
         current-value 0.0
         stock-performance {}
         cash-invested {}
         cash-invested-by-date {}
         data (rest data)]
    (if (empty? data)
      [cash portfolio portfolio-value current-value cash-invested cash-invested-by-date stock-performance]
      (let [[date action amount ticker] (first data)
            ticker-prices (client/get-ticker-price-all ticker (util/parse-date date))
            executed-date (first (first ticker-prices))				; gets the date the buy/sell order is executed
            ]
        (cond
          (= action "buy")
          (if (pos? (Double. amount))
            (let [price (second (first ticker-prices))        	; Gets open price of next trading day
                  currPrice (nth (last ticker-prices) 2)		; Gets adj close price of latest day
                  prices (mapv #(nth % 2) ticker-prices)       	; Extracts the prices from ticker-prices
                  amounts (repeatedly (count prices) #(Double. amount))	; Creates a sequence of amounts matching the number of prices
                  trading-dates (mapv #(first %) ticker-prices)
                  ]
              (recur (- cash (* (Double. amount) price))
                     (assoc portfolio ticker (+ (get portfolio ticker 0) (Double. amount)))
                     (merge-with + portfolio-value (zipmap (map first ticker-prices) (map #(- % (* (Double. amount) price))(map * prices amounts)))) ; Updates portfolio-value with the calculated values
                     (+ current-value (* (Double. amount) currPrice))
                     
                     (assoc stock-performance ticker (calculate-returns-with-corresponding-date prices trading-dates))
                     (assoc cash-invested ticker (+ (get cash-invested ticker 0) (* (Double. amount) price)))
                     (assoc cash-invested-by-date executed-date (assoc cash-invested ticker (+ (get cash-invested ticker 0) (* (Double. amount) price))))
                     (rest data)))
            (recur cash portfolio portfolio-value current-value cash-invested cash-invested-by-date stock-performance (rest data)))

          (= action "sell")
          (let [price (second (first ticker-prices))
                currPrice (second (last ticker-prices))
                prices (map second ticker-prices)
                amounts (repeatedly (count prices) #(Double. amount))
                trading-dates (mapv #(first %) ticker-prices)
                ]
            (recur (+ cash (* (Double. amount) price))
                   (assoc portfolio ticker (- (get portfolio ticker 0) (Double. amount)))
                   (merge-with + portfolio-value (zipmap (map first ticker-prices) (map #(- % (* (Double. amount) price))(map * prices amounts)))) ; Updates portfolio-value with the calculated values
                   (- current-value (* (Double. amount) currPrice))
                   
                   (assoc stock-performance ticker (calculate-returns-with-corresponding-date prices trading-dates))
                   (assoc cash-invested ticker (- (get cash-invested ticker 0) (* (Double. amount) price)))
                   (assoc cash-invested-by-date executed-date (assoc cash-invested ticker (- (get cash-invested ticker 0) (* (Double. amount) price))))
                   (rest data)))
          )))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cim_portfolio.portfolio/analyze-portfolio</span>","value":"#'cim_portfolio.portfolio/analyze-portfolio"}
;; <=

;; @@
(def portfolio-options {
                        :starting-cash 200000
                       }
)

(def view-options {
                   :show-individual-stock-performance-by-day 	false
                   :show-cumulative-portfolio-return-by-day		true
                   :show-portfolio-value-by-day					true
                  }
)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cim_portfolio.portfolio/view-options</span>","value":"#'cim_portfolio.portfolio/view-options"}
;; <=

;; @@
;; Please enter the relative paths of 1 or more CSV files (in a list) containing your trades below:

(def input-files ["examples/testPortfolio1.csv" "examples/testPortfolio.csv"])

(let [data (util/read-multiple-csv input-files)
      [cash portfolio portfolio-value current-value cash-invested cash-invested-by-date stock-performance] (analyze-portfolio data)
      sorted-portfolio-value 	(map #(vector (first %) (+ (:starting-cash portfolio-options) (second %))) (util/sort-map-by-date portfolio-value))
      cash-invested-by-dates 	(into [] cash-invested-by-date)
      current-portfolio-value 	(+ (:starting-cash portfolio-options) (+ cash current-value))
      annualized-return 		(calculate-annualized-return (:starting-cash portfolio-options) current-portfolio-value (first (first sorted-portfolio-value)) (last (last sorted-portfolio-value)))
      cumulative-portfolio-return (calculate-portfolio-return cash-invested stock-performance)
      returns-by-date 			(zipmap (map #(first %) cash-invested-by-date)(mapv #(calculate-portfolio-return-for-given-date (second %) stock-performance (first %)) cash-invested-by-dates))
      ]
  (def portfolio-value-by-day sorted-portfolio-value)
  
  (println "Current Portfolio Value: $" (format "%.2f" current-portfolio-value) "\n")
  
  (println "Annualized Return of portfolio:" (format "%.2f" (* annualized-return 100)) "%\n")
  (println "Cash On Hand (excluding value of portfolio): $" (format "%.2f"(+ (:starting-cash portfolio-options) cash)) "\n")
  (println "Portfolio (units held of each stock):" portfolio "\n")
  (println "Cash invested in each stock: " cash-invested "\n")
  (println "Cumulative Portfolio Return: " cumulative-portfolio-return "\n")
  
  (println "----------------------------------")
  (if (:show-cumulative-portfolio-return-by-day view-options) (println "Portfolio Return by date: \n" returns-by-date))
  (println "----------------------------------\n")
  
  (println "----------------------------------")
  (println "Portfolio Value Day-by-Day: ")
  (if (:show-portfolio-value-by-day view-options) 
    (run! println (map #(vector (first %) (format "%.2f" (second %))) sorted-portfolio-value))
    (println "Omitting...")
  )
  (println "----------------------------------\n")
  
  (println "----------------------------------")
  (println "Individual Stock Performance: ")
  (if (:show-individual-stock-performance-by-day view-options) 
    (clojure.pprint/pprint stock-performance)
    (println "Omitting...")
  )
  (println "----------------------------------\n")
)
;; @@
;; ->
;;; Current Portfolio Value: $ 228642.85 
;;; 
;;; Annualized Return of portfolio: 28.64 %
;;; 
;;; Cash On Hand (excluding value of portfolio): $ 23289.00 
;;; 
;;; Portfolio (units held of each stock): {IEF 100.0, NVDA 70.0, SHY 170.0, RSP 35.0, SPY 150.0, GOOG 50.0, TSLA -30.0, GSG 250.0, AGG 300.0} 
;;; 
;;; Cash invested in each stock:  {IEF 9070.99990845, NVDA 48171.300659181, SHY 13787.499618528002, RSP 4944.7999572755, SPY 65155.500793454994, GOOG 7172.49984741, TSLA -6015.600128175, GSG 5449.999809275, AGG 28974.00054933} 
;;; 
;;; Cumulative Portfolio Return:  0.07357477365465759 
;;; 
;;; ----------------------------------
;;; Portfolio Return by date: 
;;;  {2024-02-16 -0.03067546939783191, 2024-02-26 -0.00814692008853303, 2024-03-01 -0.016924292153363663, 2024-03-11 -0.013469352597299093, 2023-10-16 0.08417280828965135, 2023-11-06 0.08244980154132737, 2023-12-04 0.025756707175512594}
;;; ----------------------------------
;;; 
;;; ----------------------------------
;;; Portfolio Value Day-by-Day: 
;;; [2023-10-16 199719.79]
;;; [2023-10-17 199651.02]
;;; [2023-10-18 198954.37]
;;; [2023-10-19 198477.53]
;;; [2023-10-20 197950.91]
;;; [2023-10-23 197890.52]
;;; [2023-10-24 198250.81]
;;; [2023-10-25 197515.23]
;;; [2023-10-26 197079.11]
;;; [2023-10-27 196854.04]
;;; [2023-10-30 197347.89]
;;; [2023-10-31 197626.64]
;;; [2023-11-01 198190.49]
;;; [2023-11-02 199155.01]
;;; [2023-11-03 199697.75]
;;; [2023-11-06 199340.62]
;;; [2023-11-07 199423.71]
;;; [2023-11-08 199426.54]
;;; [2023-11-09 198742.51]
;;; [2023-11-10 199870.58]
;;; [2023-11-13 199889.37]
;;; [2023-11-14 201479.19]
;;; [2023-11-15 201510.64]
;;; [2023-11-16 201514.89]
;;; [2023-11-17 201713.47]
;;; [2023-11-20 202334.90]
;;; [2023-11-21 202223.29]
;;; [2023-11-22 202453.74]
;;; [2023-11-24 202400.48]
;;; [2023-11-27 202301.23]
;;; [2023-11-28 202506.45]
;;; [2023-11-29 202606.55]
;;; [2023-11-30 202765.29]
;;; [2023-12-01 203324.06]
;;; [2023-12-04 202579.42]
;;; [2023-12-05 202754.38]
;;; [2023-12-06 202451.64]
;;; [2023-12-07 203023.51]
;;; [2023-12-08 203149.46]
;;; [2023-12-11 203459.89]
;;; [2023-12-12 203788.71]
;;; [2023-12-13 205476.62]
;;; [2023-12-14 206213.46]
;;; [2023-12-15 205984.64]
;;; [2023-12-18 206342.16]
;;; [2023-12-19 206925.69]
;;; [2023-12-20 205971.13]
;;; [2023-12-21 206721.91]
;;; [2023-12-22 206819.80]
;;; [2023-12-26 207281.59]
;;; [2023-12-27 207659.56]
;;; [2023-12-28 207519.67]
;;; [2023-12-29 207188.32]
;;; [2024-01-02 206547.07]
;;; [2024-01-03 206029.75]
;;; [2024-01-04 205594.64]
;;; [2024-01-05 205634.34]
;;; [2024-01-08 206750.33]
;;; [2024-01-09 206665.72]
;;; [2024-01-10 206964.34]
;;; [2024-01-11 207224.35]
;;; [2024-01-12 207379.61]
;;; [2024-01-16 206725.95]
;;; [2024-01-17 206155.43]
;;; [2024-01-18 206822.58]
;;; [2024-01-19 207744.15]
;;; [2024-01-22 208051.94]
;;; [2024-01-23 208191.01]
;;; [2024-01-24 208187.90]
;;; [2024-01-25 208907.05]
;;; [2024-01-26 208804.45]
;;; [2024-01-29 209543.94]
;;; [2024-01-30 209594.31]
;;; [2024-01-31 208499.60]
;;; [2024-02-01 209681.38]
;;; [2024-02-02 209937.69]
;;; [2024-02-05 209284.33]
;;; [2024-02-06 209787.90]
;;; [2024-02-07 210376.30]
;;; [2024-02-08 210374.95]
;;; [2024-02-09 210791.29]
;;; [2024-02-12 210822.66]
;;; [2024-02-13 209274.10]
;;; [2024-02-14 210121.90]
;;; [2024-02-15 210853.14]
;;; [2024-02-16 208838.06]
;;; [2024-02-20 205276.87]
;;; [2024-02-21 203269.49]
;;; [2024-02-22 215969.85]
;;; [2024-02-23 216398.36]
;;; [2024-02-26 216098.19]
;;; [2024-02-27 215922.75]
;;; [2024-02-28 214727.36]
;;; [2024-02-29 216669.50]
;;; [2024-03-01 220685.59]
;;; [2024-03-04 223197.69]
;;; [2024-03-05 222832.05]
;;; [2024-03-06 225943.16]
;;; [2024-03-07 230791.78]
;;; [2024-03-08 225452.27]
;;; [2024-03-11 223564.07]
;;; [2024-03-12 230995.38]
;;; [2024-03-13 230670.69]
;;; [2024-03-14 226859.32]
;;; [2024-03-15 225166.60]
;;; [2024-03-18 227846.87]
;;; [2024-03-19 228298.04]
;;; [2024-03-20 231134.47]
;;; [2024-03-21 233310.41]
;;; [2024-03-22 235619.67]
;;; [2024-03-25 236934.62]
;;; [2024-03-26 235243.88]
;;; [2024-03-27 233107.04]
;;; [2024-03-28 232186.45]
;;; [2024-04-01 231997.95]
;;; [2024-04-02 229627.11]
;;; [2024-04-03 229326.50]
;;; [2024-04-04 225950.70]
;;; [2024-04-05 227690.05]
;;; [2024-04-08 227472.80]
;;; [2024-04-09 225756.20]
;;; [2024-04-10 224983.45]
;;; [2024-04-11 230310.75]
;;; [2024-04-12 227419.80]
;;; [2024-04-15 223613.90]
;;; [2024-04-16 223502.25]
;;; [2024-04-17 220407.00]
;;; [2024-04-18 219587.25]
;;; [2024-04-19 209872.25]
;;; [2024-04-22 212295.65]
;;; [2024-04-23 217237.70]
;;; [2024-04-24 215929.25]
;;; [2024-04-25 216704.65]
;;; [2024-04-26 225176.05]
;;; ----------------------------------
;;; 
;;; ----------------------------------
;;; Individual Stock Performance: 
;;; Omitting...
;;; ----------------------------------
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Visualization
;; **

;; @@
(plot/list-plot (cons (:starting-cash portfolio-options) (map #(second %) portfolio-value-by-day)) :joined true :plot-size 800 :x-title "Day" :y-title "Portfolio Value (in $)" :color "#7f3b08")
;; @@
;; =>
;;; {"type":"vega","content":{"width":800,"height":494.4376,"padding":{"top":10,"left":80,"bottom":50,"right":10},"data":[{"name":"edbf91fd-25d2-49cd-b84e-a1ee9246acf8","values":[{"x":0,"y":200000},{"x":1,"y":199719.7885131845},{"x":2,"y":199651.0173034635},{"x":3,"y":198954.37309264852},{"x":4,"y":198477.531356806},{"x":5,"y":197950.9052276615},{"x":6,"y":197890.5179595885},{"x":7,"y":198250.8127593915},{"x":8,"y":197515.234756477},{"x":9,"y":197079.1088867175},{"x":10,"y":196854.03564452552},{"x":11,"y":197347.8899383545},{"x":12,"y":197626.6374969425},{"x":13,"y":198190.4866790835},{"x":14,"y":199155.00770569252},{"x":15,"y":199697.7525329545},{"x":16,"y":199340.617065417},{"x":17,"y":199423.711853018},{"x":18,"y":199426.5393066545},{"x":19,"y":198742.51134871552},{"x":20,"y":199870.581226323},{"x":21,"y":199889.368534097},{"x":22,"y":201479.1886520395},{"x":23,"y":201510.641231521},{"x":24,"y":201514.8916435295},{"x":25,"y":201713.4737396235},{"x":26,"y":202334.9011421005},{"x":27,"y":202223.2900619515},{"x":28,"y":202453.743534096},{"x":29,"y":202400.48074721},{"x":30,"y":202301.23474122002},{"x":31,"y":202506.4465713495},{"x":32,"y":202606.5460586415},{"x":33,"y":202765.2862930145},{"x":34,"y":203324.063777907},{"x":35,"y":202579.42377087698},{"x":36,"y":202754.3835830705},{"x":37,"y":202451.6399573935},{"x":38,"y":203023.514366138},{"x":39,"y":203149.455471025},{"x":40,"y":203459.888362894},{"x":41,"y":203788.707523336},{"x":42,"y":205476.62174223748},{"x":43,"y":206213.460407234},{"x":44,"y":205984.643955204},{"x":45,"y":206342.159461943},{"x":46,"y":206925.686111427},{"x":47,"y":205971.13424299101},{"x":48,"y":206721.9080352365},{"x":49,"y":206819.795703848},{"x":50,"y":207281.5926551235},{"x":51,"y":207659.5580291395},{"x":52,"y":207519.670829762},{"x":53,"y":207188.3153343005},{"x":54,"y":206547.068691249},{"x":55,"y":206029.752540542},{"x":56,"y":205594.637699119},{"x":57,"y":205634.34457776698},{"x":58,"y":206750.3285216865},{"x":59,"y":206665.7173538055},{"x":60,"y":206964.33616638702},{"x":61,"y":207224.352073638},{"x":62,"y":207379.6069717115},{"x":63,"y":206725.947151164},{"x":64,"y":206155.4299163385},{"x":65,"y":206822.5769233425},{"x":66,"y":207744.145107241},{"x":67,"y":208051.9444656235},{"x":68,"y":208191.008911129},{"x":69,"y":208187.90016171598},{"x":70,"y":208907.0528220725},{"x":71,"y":208804.448127731},{"x":72,"y":209543.9446258485},{"x":73,"y":209594.3081664645},{"x":74,"y":208499.599170694},{"x":75,"y":209681.380271872},{"x":76,"y":209937.689456906},{"x":77,"y":209284.326496091},{"x":78,"y":209787.8993987725},{"x":79,"y":210376.2996864305},{"x":80,"y":210374.9519156995},{"x":81,"y":210791.290245012},{"x":82,"y":210822.6583480655},{"x":83,"y":209274.0996741845},{"x":84,"y":210121.904048899},{"x":85,"y":210853.1390571525},{"x":86,"y":208838.058814983},{"x":87,"y":205276.86733242648},{"x":88,"y":203269.49172969648},{"x":89,"y":215969.8539733705},{"x":90,"y":216398.3602523755},{"x":91,"y":216098.1854248155},{"x":92,"y":215922.7501869285},{"x":93,"y":214727.3605918735},{"x":94,"y":216669.49502941399},{"x":95,"y":220685.589275362},{"x":96,"y":223197.6866149695},{"x":97,"y":222832.04624173648},{"x":98,"y":225943.156204208},{"x":99,"y":230791.776733392},{"x":100,"y":225452.270832031},{"x":101,"y":223564.06599041552},{"x":102,"y":230995.3822898585},{"x":103,"y":230670.69057466398},{"x":104,"y":226859.32054521},{"x":105,"y":225166.5995597525},{"x":106,"y":227846.8660545395},{"x":107,"y":228298.037853262},{"x":108,"y":231134.465370166},{"x":109,"y":233310.407085407},{"x":110,"y":235619.6690368415},{"x":111,"y":236934.62371822697},{"x":112,"y":235243.8771628985},{"x":113,"y":233107.03752515302},{"x":114,"y":232186.45002361148},{"x":115,"y":231997.94580458148},{"x":116,"y":229627.105751031},{"x":117,"y":229326.499233233},{"x":118,"y":225950.700836171},{"x":119,"y":227690.0499725085},{"x":120,"y":227472.79533384898},{"x":121,"y":225756.1981582245},{"x":122,"y":224983.451442677},{"x":123,"y":230310.74766157},{"x":124,"y":227419.798526769},{"x":125,"y":223613.901882145},{"x":126,"y":223502.2530364895},{"x":127,"y":220406.9981384305},{"x":128,"y":219587.251930227},{"x":129,"y":209872.2490882675},{"x":130,"y":212295.649147015},{"x":131,"y":217237.69712446502},{"x":132,"y":215929.2524527985},{"x":133,"y":216704.650154109},{"x":134,"y":225176.0490989645}]}],"marks":[{"type":"line","from":{"data":"edbf91fd-25d2-49cd-b84e-a1ee9246acf8"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"stroke":{"value":"#7f3b08"},"strokeWidth":{"value":3},"strokeOpacity":{"value":1}}}}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"edbf91fd-25d2-49cd-b84e-a1ee9246acf8","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"edbf91fd-25d2-49cd-b84e-a1ee9246acf8","field":"data.y"}}],"axes":[{"type":"x","scale":"x","title":"Day","titleOffset":40,"grid":true},{"type":"y","scale":"y","title":"Portfolio Value (in $)","titleOffset":65,"grid":true}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 800, :height 494.4376, :padding {:top 10, :left 80, :bottom 50, :right 10}, :data [{:name \"edbf91fd-25d2-49cd-b84e-a1ee9246acf8\", :values ({:x 0, :y 200000} {:x 1, :y 199719.7885131845} {:x 2, :y 199651.0173034635} {:x 3, :y 198954.37309264852} {:x 4, :y 198477.531356806} {:x 5, :y 197950.9052276615} {:x 6, :y 197890.5179595885} {:x 7, :y 198250.8127593915} {:x 8, :y 197515.234756477} {:x 9, :y 197079.1088867175} {:x 10, :y 196854.03564452552} {:x 11, :y 197347.8899383545} {:x 12, :y 197626.6374969425} {:x 13, :y 198190.4866790835} {:x 14, :y 199155.00770569252} {:x 15, :y 199697.7525329545} {:x 16, :y 199340.617065417} {:x 17, :y 199423.711853018} {:x 18, :y 199426.5393066545} {:x 19, :y 198742.51134871552} {:x 20, :y 199870.581226323} {:x 21, :y 199889.368534097} {:x 22, :y 201479.1886520395} {:x 23, :y 201510.641231521} {:x 24, :y 201514.8916435295} {:x 25, :y 201713.4737396235} {:x 26, :y 202334.9011421005} {:x 27, :y 202223.2900619515} {:x 28, :y 202453.743534096} {:x 29, :y 202400.48074721} {:x 30, :y 202301.23474122002} {:x 31, :y 202506.4465713495} {:x 32, :y 202606.5460586415} {:x 33, :y 202765.2862930145} {:x 34, :y 203324.063777907} {:x 35, :y 202579.42377087698} {:x 36, :y 202754.3835830705} {:x 37, :y 202451.6399573935} {:x 38, :y 203023.514366138} {:x 39, :y 203149.455471025} {:x 40, :y 203459.888362894} {:x 41, :y 203788.707523336} {:x 42, :y 205476.62174223748} {:x 43, :y 206213.460407234} {:x 44, :y 205984.643955204} {:x 45, :y 206342.159461943} {:x 46, :y 206925.686111427} {:x 47, :y 205971.13424299101} {:x 48, :y 206721.9080352365} {:x 49, :y 206819.795703848} {:x 50, :y 207281.5926551235} {:x 51, :y 207659.5580291395} {:x 52, :y 207519.670829762} {:x 53, :y 207188.3153343005} {:x 54, :y 206547.068691249} {:x 55, :y 206029.752540542} {:x 56, :y 205594.637699119} {:x 57, :y 205634.34457776698} {:x 58, :y 206750.3285216865} {:x 59, :y 206665.7173538055} {:x 60, :y 206964.33616638702} {:x 61, :y 207224.352073638} {:x 62, :y 207379.6069717115} {:x 63, :y 206725.947151164} {:x 64, :y 206155.4299163385} {:x 65, :y 206822.5769233425} {:x 66, :y 207744.145107241} {:x 67, :y 208051.9444656235} {:x 68, :y 208191.008911129} {:x 69, :y 208187.90016171598} {:x 70, :y 208907.0528220725} {:x 71, :y 208804.448127731} {:x 72, :y 209543.9446258485} {:x 73, :y 209594.3081664645} {:x 74, :y 208499.599170694} {:x 75, :y 209681.380271872} {:x 76, :y 209937.689456906} {:x 77, :y 209284.326496091} {:x 78, :y 209787.8993987725} {:x 79, :y 210376.2996864305} {:x 80, :y 210374.9519156995} {:x 81, :y 210791.290245012} {:x 82, :y 210822.6583480655} {:x 83, :y 209274.0996741845} {:x 84, :y 210121.904048899} {:x 85, :y 210853.1390571525} {:x 86, :y 208838.058814983} {:x 87, :y 205276.86733242648} {:x 88, :y 203269.49172969648} {:x 89, :y 215969.8539733705} {:x 90, :y 216398.3602523755} {:x 91, :y 216098.1854248155} {:x 92, :y 215922.7501869285} {:x 93, :y 214727.3605918735} {:x 94, :y 216669.49502941399} {:x 95, :y 220685.589275362} {:x 96, :y 223197.6866149695} {:x 97, :y 222832.04624173648} {:x 98, :y 225943.156204208} {:x 99, :y 230791.776733392} {:x 100, :y 225452.270832031} {:x 101, :y 223564.06599041552} {:x 102, :y 230995.3822898585} {:x 103, :y 230670.69057466398} {:x 104, :y 226859.32054521} {:x 105, :y 225166.5995597525} {:x 106, :y 227846.8660545395} {:x 107, :y 228298.037853262} {:x 108, :y 231134.465370166} {:x 109, :y 233310.407085407} {:x 110, :y 235619.6690368415} {:x 111, :y 236934.62371822697} {:x 112, :y 235243.8771628985} {:x 113, :y 233107.03752515302} {:x 114, :y 232186.45002361148} {:x 115, :y 231997.94580458148} {:x 116, :y 229627.105751031} {:x 117, :y 229326.499233233} {:x 118, :y 225950.700836171} {:x 119, :y 227690.0499725085} {:x 120, :y 227472.79533384898} {:x 121, :y 225756.1981582245} {:x 122, :y 224983.451442677} {:x 123, :y 230310.74766157} {:x 124, :y 227419.798526769} {:x 125, :y 223613.901882145} {:x 126, :y 223502.2530364895} {:x 127, :y 220406.9981384305} {:x 128, :y 219587.251930227} {:x 129, :y 209872.2490882675} {:x 130, :y 212295.649147015} {:x 131, :y 217237.69712446502} {:x 132, :y 215929.2524527985} {:x 133, :y 216704.650154109} {:x 134, :y 225176.0490989645})}], :marks [{:type \"line\", :from {:data \"edbf91fd-25d2-49cd-b84e-a1ee9246acf8\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :stroke {:value \"#7f3b08\"}, :strokeWidth {:value 3}, :strokeOpacity {:value 1}}}}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"edbf91fd-25d2-49cd-b84e-a1ee9246acf8\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"edbf91fd-25d2-49cd-b84e-a1ee9246acf8\", :field \"data.y\"}}], :axes [{:type \"x\", :scale \"x\", :title \"Day\", :titleOffset 40, :grid true} {:type \"y\", :scale \"y\", :title \"Portfolio Value (in $)\", :titleOffset 65, :grid true}]}}"}
;; <=
