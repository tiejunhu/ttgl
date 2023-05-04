(ns ttgl.db)

(def default-db {:database nil
                 :items nil
                 :filters nil})

(def ^:private wood-fiber {:id "wood" :name "Wood"})

(defn- get-select-option-id-names [select]
  (when (= (:type select) "select")
    (map (fn [x] {:name (:name x)
                  :id (:id x)}) (:options (:select select)))))

(defn- get-select-option-id-name [select]
  (when (= (:type select) "select")
    {:name (:name (:select select))
     :id (:id (:select select))}))

(defn- get-title-text [title]
  (when (= (:type title) "title")
    (apply str (map :plain_text (:title title)))))

(defn- get-richtext-text [richtext]
  (when (= (:type richtext) "rich_text")
    (:plain_text (first (:rich_text richtext)))))

(defn- get-number [number]
  (when (= (:type number) "number")
    (:number number)))

(defn- parse-item [item]
  (when (= (:object item) "page")
    (let [properties (:properties item)
          ec (get-number (:EC properties))
          ep (get-number (:EP properties))
          vp (get-number (:VP properties))
          vl (get-number (:VL properties))
          pos (get-select-option-id-name (:Pos properties))
          fiber (get-select-option-id-name (:Fiber properties))]
      {:id (:id item)
       :name (get-title-text (:Name properties))
       :construct (get-select-option-id-name (:Construct properties))
       :brand (get-select-option-id-name (:Brand properties))
       :pos pos
       :fiber (if (:id fiber) fiber wood-fiber)
       :ec ec
       :ep ep
       :ecp (when (and ec ep) (/ ec ep))
       :vp vp
       :vl vl
       :vlp (when (and vl vp) (/ vl vp))
       :chinese-name (get-richtext-text (:CN properties))})))

(defn- parse-incoming-list [list func]
  {:list (map func (:results list))})

(defn parse-incoming-items [list]
  (parse-incoming-list list parse-item))

(defn parse-incoming-database [database]
  (let [properties (:properties database)]
    {:construct (get-select-option-id-names (:Construct properties))
     :pos (get-select-option-id-names (:Pos properties))
     :fiber (conj (get-select-option-id-names (:Fiber properties)) wood-fiber)
     :brand (get-select-option-id-names (:Brand properties))}))
