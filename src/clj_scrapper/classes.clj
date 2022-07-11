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

(generate-url 1443 :sem1 :male 22)
(defn- html-text-trim [dom] (s/trim (html/text dom)))
(defn- nth-html
  "Trimmed text nth node as text"
  [dom n]
  (html-text-trim (nth dom n)))
(defn- timestamp
  "Returns timestamp in 0000 form, e.g. 1430 (i.e. 2:30pm)
  `type` can be `:start` or `end`
  `dom` should be the :td element directly"
  [type dom]
  ((type {:start first, :end second}) (s/split (html-text-trim dom) #" *- *")))

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
       vec))

(defn- available
  [text]
  ({"غير متاحه" :not-available, "متاحه" :available, "ممتلئة" :full} text))

(defn parse-class
  [class-dom]
  (let [code (nth-html class-dom 0)
        crn (nth-html class-dom 1)
        section (nth-html class-dom 2)
        instructor (nth-html class-dom 9)
        days (days (nth-html class-dom 6))
        starting-time (timestamp :start (nth class-dom 8))
        ending-time (timestamp :end (nth class-dom 8))
        allowed-colleges (nth-html class-dom 11); TODO: to vec
        allowed-majors (nth-html class-dom 12); TODO: to vec
        available (available (nth-html class-dom 3))]
    {:code code,
     :crn crn,
     :section section,
     :instructor instructor,
     :days days,
     :starting-time starting-time,
     :ending-time ending-time,
     :allowed-colleges allowed-colleges,
     :allowed-majors allowed-majors,
     :availability available}))
