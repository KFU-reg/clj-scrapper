(ns clj-scrapper.http-helpers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]))


(defn get-dom
  "Given a URL, it will return html node"
  [url]
  (html/html-snippet (slurp url)))
