;; A Plan is organized as follows
;; One Html page has a single plan for one major
;; Example:
;; "https://www.kfu.edu.sa/ar/Deans/AdmissionRecordsDeanship/Documents/acadPlan/progs/p_22_بك_كهرباء_11.html"
;; Plan.html:
;;  - Semster 1
;;    + CourseID :  Name       :  Credits
;;    + 029393   :  Physics    :  3
;;    + 029393   :  Chemistry  :  3
;;    + 029393   :  Calculus I :  3
;; - Semster 2
;;    + CourseID :  Name       :  Credits
;;    + 029393   :  Physics    :  3

(ns clj-scrapper.plan
  (:require [net.cgrand.enlive-html :as html]
            [clj-scrapper.http-helpers :as http-helpers])
  (:gen-class))

(declare parse-plan parse-semster parse-course)

(defn parse-plan-from-url
  "Parses a Plan, directly from a url
  Returns: Same as
  see also `parse-plan`
  "
  [url]
  (parse-plan (http-helpers/get-dom url)));



(defn parse-plan
  "Parses a Plan, directly from an html-snippet
  Returns:
  (
    {
        :code '0999990',
        :name 'Physics 1',
        :credits 3,
        :pre-req (),
        :co-req (),
        :semster 0
    }
    {
        :code '0814144',
        ...
    }
  )
  "
  [dom];
  (->> (html/select dom [:tbody])
       (map :content)
       (map-indexed parse-semster)
       flatten))

(defn- parse-semster
  "Takes an index, and a semster html node"
  [i sem]
  (->> sem
       (map :content)
       (map parse-course)
       ; Adding the semster number
       (map #(assoc % :semster i))))

(defn- parse-course
  [course]
  (let [code (html/text (nth course 0))
        name (html/text (nth course 2))
        credits (Integer/parseInt (html/text (nth course 3)))
        pre-req () ; TODO: Pre Reqs
        co-req ()] ;TODO: Co Reqs
    {:code code,
     :name name,
     :credits credits,
     :pre-req pre-req,
     :co-req co-req}))