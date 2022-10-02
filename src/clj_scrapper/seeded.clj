;;;; This is about "seeding" plans
;;;; seeding is adding current classes to
;;;; a plan.
;;;; A plan will have the courses that one should take to gradute.
;;;; Classes are lists of classes currently `:available` (or not avaliable)
;;;; A Seeded plan will fill the courses one should take with the currently
;;;; available data
;;;; Sorry couldn't explain better :(

;;;; Maybe an epxlanation in data makes more sense?
;;;; These are `course`s of a `plan`
;;```
;; [
;;   {
;;    :code "9999999",
;;    :name "Physics",
;;    :credits 3,
;;    :pre-req (),
;;    :co-req (),
;;    :semster 0
;;   }
;; ...
;; ]
;;
;;```
;;;; These are `class`es
;;```
;; [
;;   {
;;     :availability :full,
;;     :allowed-majors ["2204"],
;;     :section "01",
;;     :allowed-colleges ["22"],
;;     :days [:tuesday],
;;     :ending-time "1715",
;;     :instructor "Name",
;;     :code "9999888",
;;     :starting-time "1430",
;;     :crn "88888"
;;  }
;; ...
;; ]
;;```
;;
;;;; The final `seeded` `plan`


(ns clj-scrapper.seeded (:gen-class))


(defn- filter-classes
  "Simply returns classes with matching `code`"
  [code classes]
  ; Match codes with "X" example: "069XXX"
  ; It should match as a wildcard "*"
  (let [regex-s (clojure.string/replace code #"X" ".")
        regex-p (re-pattern regex-s)]
    (filter #(re-matches regex-p (:code %)) classes)))

(defn- seed1course
  "seeds one course with [classes]
  Could return empty list `()` incase
  there are no classes for a certain course"
  [{code :code, :as course} classes]
  (->> ; We start by classes of a course
    (filter-classes code classes)
    ; `:code` already exists in `plan`. Removing duplicates
    (map #(dissoc % :code))
    ; then add data from course with each class
    (assoc course :classes)))


(defn- seed1plan
  "seed all courses in a [plan] with [classes]
  Example Return:
  ```
  [
   {
      :code '0999990',
      :name 'Physics 1',
      :credits 3,
      :pre-req (),
      :co-req (),
      :semster 0
      :classes ({:availability :not-available,
                 :allowed-majors [],
                 :allowed-colleges ['22'],
                 :section '03',
                 :days [:wednesday],
                 :instructor 'زياد نايف شطناوي',
                 :starting-time '0800',
                 :ending-time '0950',
                 :crn '39170'}

                 {...}
      )
   }
  ...
  ]
  ```
  "
  [plan classes] ;
  (remove empty? ; lists `()` caused by `seed1`
    (map #(seed1course % classes) plan)))


(defn seed-plans
  "For return, See: [[seed1plan]]"
  [plans classes]
  (map #(seed1plan % classes) plans))
