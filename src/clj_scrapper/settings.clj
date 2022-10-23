(ns clj-scrapper.settings
  (:require [clj-scrapper.classes :as classes]))

(def current-year 1443)
(def current-sem :sem2)

(def ^:private ^:const department-numbers
  {"10" "الطب",
   "31" "العلوم الطبية التطبيقية",
   "34" "الاسنان",
   "20" "الصيدلة الاكلينيكية",
   "03" "الطب البيطيري",
   "22" "الهندسة",
   "09" "علوم الحاسب وتقنية المعلومات",
   "06" "إدارة الأعمال",
   "08" "العلوم",
   "01" "العلوم الزراعية والاغذية",
   "02" "التربية",
   "27" "الحقوق",
   "74" "الآداب",
   "30" "الدراسات التطبيقية وخدمة المجمتع",
   "28" " الدراسات التطبيقية وخدمة المجمتع فرع بقيق",
   "00" "مركز اللغة الانجيليزية"})

(def generate-url (partial classes/generate-url current-year current-sem))

(def ^:private ^:const plans
  [{:name "Electrical Engineering",
    :url
      "https://www.kfu.edu.sa/ar/Deans/AdmissionRecordsDeanship/Documents/acadPlan/progs/p_22_%D8%A8%D9%83_%D9%83%D9%87%D8%B1%D8%A8%D8%A7%D8%A1_11.html",
    :college :Engineering,
    :gender :male}
   {:name "Computer Science",
    :url
      "https://www.kfu.edu.sa/ar/Deans/AdmissionRecordsDeanship/Documents/acadPlan/progs/p_09_%D8%A8%D9%83_%D8%B9_%D8%AD%D8%A7%D8%B3%D8%A8_11.html",
    :college :Computer_Science_and_Information_Technology,
    :gender :all}
   {:name "Chemical Engineering",
    :url
      "https://www.kfu.edu.sa/ar/Deans/AdmissionRecordsDeanship/Documents/acadPlan/progs/p_22_%D8%A8%D9%83_%D9%83%D9%8A%D9%85%D9%8A%D8%A7%D8%A6_11.html",
    :college :Engineering,
    :gender :male}])

(defn get-department-numbers [] (keys department-numbers))
(defn get-department-names [] (vals department-numbers))

; basically, converts `:all` to two entries, `:male` and `:female`
(defn get-plans
  []
  (reduce (fn [acc cur]
            (if (= :all (:gender cur))
              (conj acc ;
                    (assoc cur :gender :male)
                    (assoc cur :gender :female))
              (conj acc cur)))
    []
    plans))


(defn generate-metadata
  "
  Returns Plans, grouped-by `:gender`, then `:college`.
  Example Return:
  ([:male
    {:Engineering
     [{:name 'Electrical Engineering',
       :url 'planEE.html',
       :college :Engineering,
       :gender :male}
      {:name 'Chemical Engineering',
       :url 'che.html',
       :college :Engineering,
       :gender :male}],
     :Computer_Science_and_Information_Technology
     [{:name 'Computer Science',
       :url 'che.html',
       :college :Computer_Science_and_Information_Technology,
       :gender :male}]}]
   [:female
    {:Engineering
     [{:name 'Biomedical Engineering',
       :url 'che.html',
       :college :Engineering,
       :gender :female}],
     :Computer_Science_and_Information_Technology
     [{:name 'Computer Science',
       :url 'che.html',
       :college :Computer_Science_and_Information_Technology,
       :gender :female}]}])
  "
  []
  (->> (get-plans)
       (group-by :gender)
       ;(map group-by-college)
       (map #(assoc % 1 (group-by :college (second %))))))


(defn plan-urls
  "Plan URLS for `gender`
  `gender` can be `:male` or `:female`"
  [gender];
  (map :url (filter #(= (:gender %) gender) (get-plans))))

(defn plan-names
  "Plan URLS for `gender`
  `gender` can be `:male` or `:female`"
  [gender];
  (map :name (filter #(= (:gender %) gender) (get-plans))))


