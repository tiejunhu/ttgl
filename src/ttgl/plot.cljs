(ns ttgl.plot
  (:require
   [reagent.core :as reagent]
   ["@antv/g2plot" :as g2plot]
   [goog.object :as gobject]
   [goog.string :as gstring]
   [goog.string.format]))

(defn component [key props]
  (reagent/create-class
   {:component-did-mount (fn [this]
                           (let [plot (g2plot/Scatter. key (clj->js props))]
                             (.render plot)
                             (gobject/set this key plot)))
    :component-did-update (fn [this]
                            (let [new-argv (rest (reagent/argv this))
                                  new-props (last new-argv)
                                  plot (gobject/get this key)]
                              (.update plot (clj->js new-props))))
    :reagent-render (fn [props]
                      [:div {:key key :id key :style {:height "calc(100vh - 41px - 24px)"}}])}))

(def plot-meta {:full-name {:alias "Full Name"}
                :full-chinese-name {:alias "中文名称"}
                :full-construct {:alias "Construction"}
                :ep {:alias "Primary Elasticity Index(Ep)"
                     :formatter (fn [v] (gstring/format "%.2f" v))}
                :ecp {:alias "Ec/Ep"
                      :formatter (fn [v] (gstring/format "%.2f" v))}
                :ec {:alias "Central Elasticity Index(Ec)"
                     :formatter (fn [v] (gstring/format "%.2f" v))}
                :vp {:alias "Primary Vibration Index(Vp)"
                     :formatter (fn [v] (gstring/format "%.2f" v))}
                :vlp {:alias "Vl/Vp"
                      :formatter (fn [v] (gstring/format "%.2f" v))}
                :vl {:alias "Lateral Vibration Index(Vl)"
                     :formatter (fn [v] (gstring/format "%.2f" v))}})

(defn x-axis [props]
  (let [x-axis-min (:min (:x-axis props))
        x-axis-max (:max (:x-axis props))
        x-axis-interval (:interval (:x-axis props))
        x-axis-text (:text (:x-axis props))]
    {:min x-axis-min
     :max x-axis-max
     :nice true
     :title {:text x-axis-text
             :position "center"}
     :tickInterval x-axis-interval
     :grid {:line {:style {:lineDash [2 2]
                           :stroke "#d0d0d0"
                           :strokeOpacity 0.8}}}}))

(defn y-axis [props]
  (let [y-axis-min (:min (:y-axis props))
        y-axis-max (:max (:y-axis props))
        y-axis-interval (:interval (:y-axis props))
        y-axis-text (:text (:y-axis props))]
    {:min y-axis-min
     :max y-axis-max
     :nice true
     :title {:text y-axis-text
             :position "end"}
     :tickInterval y-axis-interval
     :grid {:line {:style {:lineDash [2 2]
                           :stroke "#d0d0d0"
                           :strokeOpacity 0.8}}}}))

(defn annotations [props]
  (let [region-u1-text (:text (:region-u1 props))
        region-u1-text-position (:position (:region-u1 props))
        region-u2-text (:text (:region-u2 props))
        region-u2-text-position (:position (:region-u2 props))
        region-d1-text (:text (:region-d1 props))
        region-d1-text-position (:position (:region-d1 props))
        region-d2-text (:text (:region-d2 props))
        region-d2-text-position (:position (:region-d2 props))]
    [{:type "region"
      :start ["min" "1"]
      :end ["max" "1.1"]
      :style {:fill "l(90) 0:#fff7ae 1:#a0ddff"
              :fillOpacity 0.4}}
     {:type "text"
      :content region-u1-text
      :position region-u1-text-position}
     {:type "line"
      :start ["min" "1.1"]
      :end ["max" "1.1"]}
     {:type "region"
      :start ["min" "1.1"]
      :end ["max" "max"]
      :style {:fill "l(90) 0:#ffffff 1:#fff7ae"
              :fillOpacity 0.4}}
     {:type "text"
      :content region-u2-text
      :position region-u2-text-position}
     {:type "region"
      :start ["min" "0.9"]
      :end ["max" "1"]
      :style {:fill "l(270) 0:#fff7ae 1:#a0ddff"
              :fillOpacity 0.4}}
     {:type "text"
      :content region-d1-text
      :position region-d1-text-position}
     {:type "line"
      :start ["min" "0.9"]
      :end ["max" "0.9"]}
     {:type "region"
      :start ["min" "min"]
      :end ["max" "0.9"]
      :style {:fill "l(270) 0:#ffffff 1:#fff7ae"
              :fillOpacity 0.4}}
     {:type "text"
      :content region-d2-text
      :position region-d2-text-position}
     {:type "line"
      :start ["min" "1"]
      :end ["max" "1"]}]))
