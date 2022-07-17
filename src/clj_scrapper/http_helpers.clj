(ns clj-scrapper.http-helpers
  (:require [net.cgrand.enlive-html :as html]
            [org.httpkit.client :as http]))

(defn get-dom
  "Given a URL, it will return html node"
  [url]
  (html/html-snippet (slurp url)))
