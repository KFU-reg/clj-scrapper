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
  ; TODO: Match codes with "X" example: "069XXX"
  ;       It should match as a wildcard "*"
  [code classes]
  (filter #(= code (:code %)) classes))

(defn- seed1
  "seeds one course with [classes]
  Could return empty list `()` incase
  there are no classes for a certain course"
  [{code :code, :as course} classes]
  (->> ; We start by classes of a course
    (filter-classes code classes)
    ; then merge data from course with each class
    (map #(merge course %))))


(defn seed
  "seed all courses in a [plan] with [classes]"
  [plan classes] ;
  (remove empty? ; lists `()` caused by `seed1`
    (map #(seed1 % classes) plan)))
