(ns clj-scrapper.classes
  (:require [net.cgrand.enlive-html :as html]
            [clj-scrapper.http-helpers :as http-helpers]
            [clojure.string :as s]))

(def base-url "https://banner.kfu.edu.sa:7710/KFU/ws?")
(def current-year 1443)
(def current-semster 1443)

(defn generate-url
  "Generates a url from the given arguments.
  It is advised to use `partial`ly apply the first two arguments.
  ALL Arguments are Strings!

  `current-year`: in Gregorian, e.g. 1443
  `current-semster`: anything from `[:sem1, :sem2, :summer]`
                   THIS ONLY APPLIES TO 2 SEMSTERS PER YEAR!
  `sex`: `:female` or `:male`
  `code`: Arbitrary code for each colege.

  Example Return:
   'https://banner.kfu.edu.sa:7710/KFU/ws?p_trm_code=144310&p_col_code=22&p_sex_code=11'"
  [current-year current-semster sex code]
  (str base-url
       "p_trm_code="
       current-year
       (current-semster {:sem1 10, :sem2 20, :summer 30})
       "&p_col_code=" code
       "&p_sex_code=" (sex {:male 11, :female 12})))

(declare parse-class)
(defn parse-classes
  [dom]
  (->> ;; Note: This will contain elements outside the table
    ;;
    (html/select dom [:tr])
    (map :content)
    ;; 27 is the golder number!
    ;; jk. rows with 27 thingies are classes
    (filter #(= 27 (count %)))
    (map parse-class)
    ;; this removes `nil`, caused by table haeders
    ;; see [[parse-class]]
    (filter identity)
    ;; some classes have same CRN but different days.
    ;; merge days only!
    (group-by :crn)
    vals
    (map #(reduce merge-days-classes %))
    flatten))

(defn- merge-days-classes;
  ([a] a)
  ([a b] (update a :days into (:days b))))

(defn- html-text-trim [dom] (s/trim (html/text dom)))
(defn- nth-html
  "Trimmed text nth node as text"
  [dom n]
  (html-text-trim (nth dom n)))
(defn- timestamp
  "Returns timestamp in 0000 form, e.g. 1430 (i.e. 2:30pm)
  `type` can be `:start` or `end`
  `stamp` should be the string '1400 - 1700' (this is jus an exmaple)"
  [type stamp]
  ((type {:start first, :end second}) (s/split stamp #" *- *")))

(defn- days
  [dom]
  (->> dom
       (map {\ح :sunday,
             \ن :monday,
             \ث :tuesday,
             \ر :wednesday,
             \خ :thursday,
             \ج :friday,
             \س :saturday})
       ;; remove `nil` days caused by spaces
       (filter identity)
       vec))

(defn- available
  [text]
  ({"غير متاحه" :not-available, "متاحه" :available, "ممتلئة" :full} text))
(defn- allowed [text] (if (empty? text) [] (s/split text #"\s")))

;!zprint {:format :skip}
(defn parse-class
  [class-row]; technically, class-dom is a seq of td (table data)
  (let [class-dom        (html/select class-row [:td])
        code             (nth-html class-dom 0)
        crn              (nth-html class-dom 1)
        section          (nth-html class-dom 2)
        instructor       (nth-html class-dom 9)
        days             (days      (nth-html class-dom 6))
        starting-time    (timestamp :start (nth-html class-dom 8))
        ending-time      (timestamp :end   (nth-html class-dom 8))
        allowed-colleges (allowed   (nth-html class-dom 11))
        allowed-majors   (allowed   (nth-html class-dom 12))
        availability     (available (nth-html class-dom 3))]
    ;;RANT! Table headers are assigned the same as table data
    ;; website dev should have used "theader" :(
    ;;
    ;; If crn = "CRN" and not a number (as a string),
    ;; then send `nil` to be filtered later
    (if (re-find #".*CRN.*" crn) 
      nil
      {:code              code,
       :crn               crn,
       :section           section,
       :instructor        instructor,
       :days              days,
       :starting-time     starting-time,
       :ending-time       ending-time,
       :allowed-colleges  allowed-colleges,
       :allowed-majors    allowed-majors,
       :availability      availability})))

