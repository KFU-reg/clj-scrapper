(ns clj-scrapper.classes)

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
