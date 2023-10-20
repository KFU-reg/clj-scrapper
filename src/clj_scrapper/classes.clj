(ns clj-scrapper.classes
  (:require [net.cgrand.enlive-html :as html]
            [clj-scrapper.io-helpers :as io-helpers]
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
       (current-semster {:sem1 10, :sem2 20, :sem3 25, :summer 30})
       "&p_col_code=" code
       "&p_sex_code=" (sex {:male 11, :female 12})))

;; the current department during parsing
;; see [[set-department]] for explanation!
(def *current-department* (ThreadLocal.))

(declare parse-class merge-days-classes set-department!)
(defn parse-classes
  [dom]
  (->>
    ;; Note: This will contain elements outside the table
    (html/select dom [:tr])
    (map :content)
    (map set-department!);; see [[set-department!]] for explantion
    ;; 27 is the golden number! jk. rows
    ;; with 27 thingies are classes, others are table headers
    (filter #(= 27 (count %)))
    (map parse-class)
    ;; this removes `nil`, caused by table haeders
    ;; see [[parse-class]]
    (filter identity)
    ;; some classes have same CRN but different days.
    ;; merge days only!
    (group-by :crn)
    vals; keys are CRN, we need the grouped classes
    ;; each group gets `reduce`d to one class
    (map #(reduce merge-days-classes %))
    ;; flattens the [({...}), ({...})] to [{...}, {...}]
    flatten))

(declare nth-html department)

;; HTML looks like this:
; Page: 1
; College: Engineering | Department: Electrical Engineering <- 2 columns
; Subjects | Dr |  Time | ........ | 27 cols
; Circuits | ...| ..... | ........ | 27 cols
; .......................
; Page: 2
; College: Engineering | Department: Chemical Engineering <- 2 columns
;; ...........
; So, we parse in one by one, when we meet
; a Department we 'save' it into `*current-department*`,
(defn- set-department!; القسم
  [row]; the row might not contain the department
  (if (not= 27 (count row)); so we check first if it isn't a class
    (let [department (-> (html/select row [:td :p :font])
                         (nth-html 1 nil)
                         department)]
      (if (not= "" department) (.set *current-department* department))))
  row)

(defn- department
  [text] ; القسم  : الدراسات الاكلينيكية
  (s/trim (clojure.string/replace text #"القسم  :" "")))

; days are sets, so no need to worry about conflicts
(defn- merge-days-classes;
  ([a] a)
  ([a b] (update a :days into (:days b))))

(defn- html-text-trim [dom] (s/trim (html/text dom)))
(defn- nth-html
  "Returns the text at the index of nth node.
  nth-html throws an exception unless not-found is supplied."
  ([dom n] (html-text-trim (nth dom n)))
  ([dom n not-found] (html-text-trim (nth dom n not-found))))

(defn- code [text] (clojure.string/replace text "-" ""))
(defn- timestamp
  "Returns timestamp in 0000 form, e.g. 1430 (i.e. 2:30pm)
  `type` can be `:start` or `end`
  `stamp` should be the string '1400 - 1700' (this is jus an exmaple)"
  [type stamp]
  ((type {:start first, :end second}) (s/split stamp #" *- *")))

;!zprint {:format :skip}
(defn- days
  [dom]
  (->> dom
       ; Removes \n and other blanks that would other wise cause a `nil`
       ; in the  map
       (remove (comp clojure.string/blank? str))
       (map {\ح #_:sunday    1,
             \ن #_:monday    2,
             \ث #_:tuesday   3,
             \ر #_:wednesday 4,
             \خ #_:thursday  5 ,
             \ج #_:friday    6,
             \س #_:saturday  7})
       set))

(defn- available
  [text]
  ({"غير متاحه" :not-available, "متاحه" :available, "ممتلئة" :full} text))
(defn- allowed [text] (if (empty? text) [] (s/split text #"\s")))

;!zprint {:format :skip}
(defn parse-class
  [class-row]; technically, class-dom is a seq of td (table data)
  (let [class-dom        (html/select class-row [:td])
        code             (code (nth-html class-dom 0))
        crn              (nth-html class-dom 1)
        name             (nth-html class-dom 4) ;i.e class name
        credits          (nth-html class-dom 5)
        section          (nth-html class-dom 2)
        instructor       (nth-html class-dom 9)
        days             (days      (nth-html class-dom 6))
        starting-time    (timestamp :start (nth-html class-dom 8))
        ending-time      (timestamp :end   (nth-html class-dom 8))
        time             (str starting-time "-" ending-time)
        allowed-colleges (allowed   (nth-html class-dom 11))
        allowed-majors   (allowed   (nth-html class-dom 12))
        available        (available (nth-html class-dom 3))
        department       (.get *current-department*)]
    ;;RANT! Table headers are assigned the same as table data
    ;; website dev should have used "theader" :(
    ;;
    ;; If crn = "CRN" and not a number (as a string),
    ;; then send `nil` to be filtered later
    (if (re-find #".*CRN.*" crn)
      nil
      {:code              code,
       :crn               crn,
       :name              name,
       :credits           credits,
       :section           section,
       :instructor        instructor,
       :days              days,
       :time              time,
       :allowed-colleges  allowed-colleges,
       :allowed-majors    allowed-majors,
       :available         available
       :department        department})))


(defn parse-classes-from-url [url] (parse-classes (io-helpers/get-dom url)))
