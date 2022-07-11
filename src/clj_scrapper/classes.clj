(ns clj-scrapper.classes)

(def base-url "https://banner.kfu.edu.sa:7710/KFU/ws?")
(def current-year 1443)
(def current-semster 1443)

(defn generate-url
  "Generates a url from the given arguments.
  It is advised to use `partial`ly apply the first two arguments.
  ALL Arguments are Strings!

  `current-year`: in Gregorian, e.g. 1443
  `current-semster`: 10 = Sem1, 20 = Sem2, 30 = Summer.
                   THIS ONLY APPLIES TO 2 SEMSTERS PER YEAR!
  `code`: Arbitrary code for each colege.
  `sex`: `:female` or `:male`

  Example Return:
   'https://banner.kfu.edu.sa:7710/KFU/ws?p_trm_code=144310&p_col_code=22&p_sex_code=11'"
  [current-year current-semster code sex]
  (str base-url
       "p_trm_code="
       current-year
       current-semster
       "&p_col_code=" code
       "&p_sex_code=" (sex {:male 11, :female 12})))

(generate-url 1443 10 22 :male)
