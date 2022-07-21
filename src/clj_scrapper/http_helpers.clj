(ns clj-scrapper.http-helpers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]
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
