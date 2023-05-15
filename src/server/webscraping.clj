(ns server.webscraping
  (:require [net.cgrand.enlive-html :as html]
    ;[org.httpkit.client :as http]
            [clj-http.client :as http]
            )
  )

(defn get-dom
  []
  (html/html-snippet
    (:body (http/get "https://www.sharpspixley.com/"))))