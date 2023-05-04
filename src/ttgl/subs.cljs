(ns ttgl.subs
  (:require
   [re-frame.core :as rc]))

(rc/reg-sub
 :sub.data/database
 (fn [db]
   (get-in db [:database])))

(rc/reg-sub
 :sub.data/sorted-construct
 :<- [:sub.data/database]
 (fn [database _]
   (sort-by :name (:construct database))))

(rc/reg-sub
 :sub.data/sorted-fiber-pos
 :<- [:sub.data/database]
 (fn [database _]
   (sort-by :name (:pos database))))

(rc/reg-sub
 :sub.data/sorted-fiber-type
 :<- [:sub.data/database]
 (fn [database _]
   (sort-by :name (:fiber database))))

(rc/reg-sub
 :sub.data/sorted-brands
 :<- [:sub.data/database]
 (fn [database _]
   (sort-by :name (:brand database))))

(rc/reg-sub
 :sub.data/items
 (fn [db]
   (get-in db [:items :list])))

(rc/reg-sub
 :sub.data/filters
 (fn [db]
   (get-in db [:filters])))

(defn- filtered? [filters key map]
  (let [id (:id (key map))]
    (get filters (keyword id) false)))

(rc/reg-sub
 :sub.data/filtered-items
 :<- [:sub.data/items]
 :<- [:sub.data/filters]
 (fn [[items filters] _]
   (filter (fn [i]
             (let [brand-filtered? (filtered? filters :brand i)
                   construct-filtered? (filtered? filters :construct i)
                   pos-filtered? (filtered? filters :pos i)
                   fiber-filtered? (filtered? filters :fiber i)]
               ;; any of the filters are true, return false to filter out item
               (not (or brand-filtered? construct-filtered? pos-filtered? fiber-filtered?)))) items)))

(rc/reg-sub
 :sub.data/items-data
 :<- [:sub.data/filtered-items]
 (fn [items _]
   (map (fn [i]
          (let [brand (:name (:brand i))
                construct (:name (:construct i))
                pos (:name (:pos i))
                fiber (:name (:fiber i))
                chinese-name (:chinese-name i)
                name (:name i)]
            {:name (:name i)
             :full-name (str brand " " name)
             :full-chinese-name (if chinese-name (str brand " " chinese-name) "")
             :full-construct (str construct " "  pos " " fiber)
             :chinese-name chinese-name
             :brand brand
             :construct construct
             :pos pos
             :fiber fiber
             :ec (:ec i)
             :ep (:ep i)
             :ecp (:ecp i)
             :vl (:vl i)
             :vp (:vp i)
             :vlp (:vlp i)})) items)))

(rc/reg-sub
 :sub.ui/loading
 :<- [:sub.data/database]
 :<- [:sub.data/items]
 (fn [items _]
   (reduce #(or %1 %2) false (map nil? items))))
