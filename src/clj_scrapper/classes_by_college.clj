(ns clj-scrapper.classes-by-college
  (:require [clj-scrapper.plan :as plan]
            [clj-scrapper.classes :as classes]
            [clj-scrapper.settings :as settings]
            [clojure.java.io :as io]
            [clj-scrapper.seeded :as seed]
            [clj-scrapper.io-helpers :as io-helpers]
            [clojure.data.json :as json])
  (:gen-class))

(declare download>save metadata)
(defn -main
  " ... "
  [& _args]
  (download>save "./output/by_college/male/" :male)
  (download>save "./output/by_college/female/" :female)
  (metadata "./output/by_college/metadata.json")
  (shutdown-agents))

(defn metadata
  [path]
  (io-helpers/save-data-to-path-as-json
    path
    (zipmap (settings/get-department-names) (settings/get-department-numbers))))

(defn download>save
  [output-path gender]
  (let [department-names (settings/get-department-names)
        output-paths (map #(str output-path %1 ".json") department-names)
        classes (->> (settings/get-department-numbers)
                     ; [22 10 32]
                     (map #(settings/generate-url gender %))
                     ; ["https://example.com/1" "https://..."]
                     (pmap classes/parse-classes-from-url)
                     ; [[{:code "99" :name "Physics" ...}, {...}, ...], [{}]]
                     ;flatten
                     ; [{:code "99" :name "Physics" ...}, {...}, ...]
                     #_.)]; just to align comments :D
    ; {:engineering [{...} {...}]}
    (dorun (map io-helpers/save-data-to-path-as-json output-paths classes))))






