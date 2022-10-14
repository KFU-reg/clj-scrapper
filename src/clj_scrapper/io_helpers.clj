(ns clj-scrapper.io-helpers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))




(defn caching-slurp
  "Exactly like slurp...
  ... but it caches!11!!1!!"
  [url]
  (let [sanitized_url (s/replace url #"(https?://|/)" "_")
        tmp_path (str "./output/cache/" sanitized_url)]
    (try (slurp tmp_path); try slurping the cached file
         (catch java.io.FileNotFoundException _
           ; if not cached, cache it...
           (do (io/make-parents tmp_path); mkdir -p
               (spit tmp_path (slurp url));
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
  (spit output_path (json/write-str plan)))

;; (defn save-data-to-path-as-json
;;   [output_paths plans]
;;   (dorun (map save-single-data-to-path-as-json output_paths plans)))
