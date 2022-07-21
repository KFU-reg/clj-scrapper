(ns clj-scrapper.core
  (:require ;[net.cgrand.enlive-html :as html]
            ;[org.httpkit.client :as http]
    [clj-scrapper.plan :as plan]
    [clj-scrapper.classes :as classes]
    [clj-scrapper.settings :as settings]
    [clojure.java.io :as io]
    [clj-scrapper.seeded :as seed]
    [clojure.data.json :as json])
  (:gen-class))

(declare download>seed>save)


;!zprint {:format :skip}
(defn -main
  " ... "
  [& _args]
  (download>seed>save "./output/male/" :male)
  (download>seed>save "./output/female/" :female)
  (spit               "./output/metadata.json" (json/write-str (settings/generate-metadata))))

(def generate-url (partial classes/generate-url 1443 :sem1))

(defn download>seed>save
  "`output_path` is the output path to save, duh...
  `gender` can be `:male` or `:female`.
  Returns a seq of seeded plans.
  see: [[clj-scrapper.seeded/seed-plans]]

  1. It Generates URL based on `gender`, for both classes and courses (in plans).
  2. Seedes them
  3. Convert to JSON
  4. Save to `output_path`
  "
  [output_path gender]
  (let [classes (->> (settings/get-department-numbers)
                     ; [22 10 32]
                     (map #(generate-url gender %))
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
        seeded-plans-with-output-path
          ; mapped to [{:output-path  "./output/male/mechincal-engineering.json"
          ;             :plan [{...}, {...}]}]
          (map #(hash-map :output-path (str output_path %1 ".json") :plan %2)
            plan-names
            seeded-plans)]
    ; print output path.
    (map (comp println :output-path) seeded-plans-with-output-path)
    ; and save to the output path.
    (map (fn [{output_path :output-path, plan :plan}]
           ; mkdir -p
           (io/make-parents output_path)
           (spit output_path (json/write-str plan)))
      seeded-plans-with-output-path)))
