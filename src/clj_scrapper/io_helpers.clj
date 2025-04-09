(ns clj-scrapper.io-helpers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

; This was added because github runners sometimes fails!!
; Syntax error (SocketException) compiling at (...)
(defn fetch-with-retries
  [url]
  (http/get url
            {:socket-timeout 10000, ; 10 seconds
             :conn-timeout 10000,
             :retry-handler (fn [ex try-count http-context]
                              (println "Retrying due to:" (class ex))
                              (if (< try-count 3)
                                (do (Thread/sleep (* 1000 try-count)) true)
                                false))}))


(defn caching-slurp
  "Exactly like slurp...
  ... but it caches!11!!1!!"
  [url]
  (let [sanitized_url (s/replace url #"(https?://|/)" "_")
        tmp_path (str "./output/cache/" sanitized_url)]
    (try (slurp tmp_path) ; try slurping the cached file
         (catch java.io.FileNotFoundException _
           ; if not cached, cache it...
           (do (io/make-parents tmp_path) ; mkdir -p
               (spit tmp_path (fetch-with-retries url) :encoding "UTF-8") ;
               (println "Wasn't cached:(")
               ; ...and re-slurp it.
               (slurp tmp_path))))))



(defn get-dom
  "Given a URL, it will return html node"
  [url]
  (println (str "Getting " url))
  (html/html-snippet (caching-slurp url)))


(defn save-data-to-path-as-json
  [output_path plan]
  ; mkdir -p
  (io/make-parents output_path)
  (println "Saving to" output_path)
  (spit output_path
        (json/write-str plan :escape-unicode false)
        :encoding
        "UTF-8"))

;; (defn save-data-to-path-as-json
;;   [output_paths plans]
;;   (dorun (map save-single-data-to-path-as-json output_paths plans)))
