(ns clj-scrapper.classes-by-plan
  (:require ;[net.cgrand.enlive-html :as html]
            ;[org.httpkit.client :as http]
    [clj-scrapper.plan :as plan]
    [clj-scrapper.classes :as classes]
    [clj-scrapper.settings :as settings]
    [clj-scrapper.io-helpers :as io-helpers]
    [clj-scrapper.seeded :as seed]
    [clojure.data.json :as json])
  (:gen-class))

(declare download>seed>save)


;!zprint {:format :skip}
(defn -main
  " ... "
  [& _args]
  (download>seed>save "./output/by_plan/male/" :male)
  (download>seed>save "./output/by_plan/female/" :female)
  (spit               "./output/by_plan/metadata.json" (json/write-str (settings/generate-metadata)))
  (shutdown-agents))

(defn download>seed>save
  "`output-path` is the output path to save, duh...
  `gender` can be `:male` or `:female`.
  Returns a seq of seeded plans.
  see: [[clj-scrapper.seeded/seed-plans]]

  1. It Generates URL based on `gender`, for both classes and courses (in plans).
  2. Seedes them
  3. Convert to JSON
  4. Save to `output_path`
  "
  [output-path gender]
  (let [classes (->> (settings/get-department-numbers)
                     ; [22 10 32]
                     (map #(settings/generate-url gender %))
                     ; ["https://example.com/1" "https://..."]
                     (pmap classes/parse-classes-from-url)
                     ; [[{:code "99" :name "Physics" ...}, {...}, ...], [{}]]
                     flatten
                     ; [{:code "99" :name "Physics" ...}, {...}, ...]
                     #_.); just to align comments :D
        plans (->> (settings/plan-urls gender)
                   ; ["https://planEE.html" "ME.html" "BME.html"]
                   (pmap plan/parse-plan-from-url)
                   ;; see [[plan/parse-plan]]
                   #_.)
        seeded-plans (seed/seed-plans plans classes)
        plan-names (settings/plan-names gender); used in output path
        output-paths (map #(str output-path %1 ".json") plan-names)]
    ; and save to the output path.
    (dorun
      (map io-helpers/save-data-to-path-as-json output-paths seeded-plans))))
