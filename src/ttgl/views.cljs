(ns ttgl.views
  (:require
   ["@antv/g2plot" :as g2plot]
   [cljs.pprint :refer [pprint]]
   [goog.string :as gstring]
   [goog.object :as gobject]
   [goog.string.format] ;; [goog.object :as gobject]
   [re-frame.core :as rc]
   [reagent.core :as reagent]))

(def ^:private left-drawer-width 160)

(defn- left-drawer-constructs [sub-key filters]
  (let [constructs (rc/subscribe [sub-key])]
    [:ul {:class "menu-list"}
     (map (fn [c]
            (let [id (:id c)]
              [:li {:key id} [:label {:class "checkbox px-3"}
                              [:input {:type "checkbox"
                                       :class "mx-1"
                                       :readOnly true
                                       :on-click (fn [e]
                                                   (let [checked (.. e -target -checked)]
                                                     (rc/dispatch [(if checked :evt.db/remove-filter :evt.db/add-filter) id])))
                                       :checked (not (get-in filters [(keyword id)]))}]
                              (:name c)]]))
          @constructs)]))

(defn- left-drawer []
  (let [filters (rc/subscribe [:sub.data/filters])]
    [:aside {:class "menu" :style {:width (str left-drawer-width "px")}}
     [:p {:class "menu-label"} "Brands"]
     (left-drawer-constructs :sub.data/sorted-brands @filters)
     [:p {:class "menu-label"} "Construction"]
     (left-drawer-constructs :sub.data/sorted-construct @filters)
     [:p {:class "menu-label"} "Fiber Type"]
     (left-drawer-constructs :sub.data/sorted-fiber-type @filters)]))

(defn- loading-panel []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :justify-content "center"
                 :align-items "center"
                 :text-align "center"
                 :min-height "100vh"}}
   [:progress {:class "progress is-large is-info" :max "100" :style {:width "60%"}} "60%"]
   [:h4 {:class "subtitle is-4"} "Loading ..."]])

(defn- plot-component [key props]
  (reagent/create-class
   {:component-did-mount (fn [this]
                           (pprint "mounted")
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

(defn- draw-index-plot [plot-name props]
  (let [items (rc/subscribe [:sub.data/items-data])
        x-field (:x-field props)
        y-field (:y-field props)
        size-field (:size-field props)
        tooltip-fields (:tooltip-fields props)
        region-u1-text (:text (:region-u1 props))
        region-u1-text-position (:position (:region-u1 props))
        region-u2-text (:text (:region-u2 props))
        region-u2-text-position (:position (:region-u2 props))
        region-d1-text (:text (:region-d1 props))
        region-d1-text-position (:position (:region-d1 props))
        region-d2-text (:text (:region-d2 props))
        region-d2-text-position (:position (:region-d2 props))
        x-axis-min (:min (:x-axis props))
        x-axis-max (:max (:x-axis props))
        x-axis-interval (:interval (:x-axis props))
        x-axis-text (:text (:x-axis props))
        y-axis-min (:min (:y-axis props))
        y-axis-max (:max (:y-axis props))
        y-axis-interval (:interval (:y-axis props))
        y-axis-text (:text (:y-axis props))]
    [plot-component plot-name {:data @items
                               :autoFit true
                               :xField x-field
                               :yField y-field
                               :sizeField size-field
                               :size [4 8]
                               :colorField "brand"
                               :shapeField "construct"
                               :shape ["circle" "square" "diamond" "triangle"]
                               :tooltip {:fields tooltip-fields}
                               :pointStyle {:fillOpacity 1 :lineWidth 2}
                               :legend {:position "top"}
                               :annotations [{:type "region"
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
                                              :end ["max" "1"]}]
                               :xAxis {:min x-axis-min
                                       :max x-axis-max
                                       :nice true
                                       :title {:text x-axis-text
                                               :position "center"}
                                       :tickInterval x-axis-interval
                                       :grid {:line {:style {:lineDash [2 2]
                                                             :stroke "#d0d0d0"
                                                             :strokeOpacity 0.8}}}}
                               :yAxis {:min y-axis-min
                                       :max y-axis-max
                                       :nice true
                                       :title {:text y-axis-text
                                               :position "end"}
                                       :tickInterval y-axis-interval
                                       :grid {:line {:style {:lineDash [2 2]
                                                             :stroke "#d0d0d0"
                                                             :strokeOpacity 0.8}}}}
                               :meta {:full-name {:alias "Full Name"}
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
                                           :formatter (fn [v] (gstring/format "%.2f" v))}}}]))

(defn- draw-plot []
  (let [active-tab (rc/subscribe [:sub.ui/active-tab])]
    (cond
      (= @active-tab :ecp) (draw-index-plot "ecp-plot" {:x-field "ep"
                                                        :y-field "ecp"
                                                        :size-field "ec"
                                                        :tooltip-fields ["full-name" "full-chinese-name" "ep" "ec" "ecp" "full-construct"]
                                                        :region-u1 {:text "Mild Kick"
                                                                    :position [".83" "1.05"]}
                                                        :region-u2 {:text "Strong Kick"
                                                                    :position ["0.83" "1.35"]}
                                                        :region-d1 {:text "Mild Hold"
                                                                    :position ["0.83" "0.95"]}
                                                        :region-d2 {:text "Deep Hold"
                                                                    :position ["0.83" "0.75"]}
                                                        :x-axis {:min 0.8
                                                                 :max 3.8
                                                                 :text "Slower   ⇦   Primary Elasticity Index(Ep)   ⇨   Faster"
                                                                 :interval 0.2}
                                                        :y-axis {:min 0.7
                                                                 :max 1.6
                                                                 :text "Ec/Ep"
                                                                 :interval 0.1}})
      (= @active-tab :vlp) (draw-index-plot "vlp-plot" {:x-field "vp"
                                                        :y-field "vlp"
                                                        :size-field "vl"
                                                        :tooltip-fields ["full-name" "full-chinese-name" "vp" "vl" "vlp" "full-construct"]
                                                        :region-u1 {:text "Near uniform.\nA bit sharper at finger.\nA bit softer at palm."
                                                                    :position [".91" "1.05"]}
                                                        :region-u2 {:text "Relatively sharper at finger.\nRelatively softer at palm."
                                                                    :position [".91" "1.2"]}
                                                        :region-d1 {:text "Near uniform.\nA bit more comfortable at finger.\nA bit harder at palm."
                                                                    :position [".91" "0.95"]}
                                                        :region-d2 {:text "Relatively more comfortable at finger.\nRelatively harder at palm."
                                                                    :position ["0.91" "0.85"]}
                                                        :x-axis {:min 0.9
                                                                 :max 1.6
                                                                 :text "Softer   ⇦   Primary Vibration Index(Vp)   ⇨   Harder"
                                                                 :interval 0.05}
                                                        :y-axis {:min 0.8
                                                                 :max 1.3
                                                                 :text "Vl/Vp"
                                                                 :interval 0.1}}))))

(defn- tabs []
  (let [active-tab (rc/subscribe [:sub.ui/active-tab])]
    [:nav {:class "level mb-0"}
     [:div {:class "level-left"}
      [:div {:class "tabs"}
       [:ul
        [:li (when (= @active-tab :ecp) {:class "is-active"})
         [:a {:href "#" :on-click (fn [] (rc/dispatch [:evt.ui/set-active-tab :ecp]))} "Ec/Ep"]]
        [:li (when (= @active-tab :vlp) {:class "is-active"})
         [:a {:href "#" :on-click (fn [] (rc/dispatch [:evt.ui/set-active-tab :vlp]))} "Vl/Vp"]]]]]
     [:div {:class "level-right"}
      [:a {:href "https://ttgearlab.com/2017/02/06/performance-indices-the-way-to-evaluatie-blade-by-measurement/"
           :target "_blank"} "About Ec/Ep Vl/Vp"]]]))

(defn- main-view []
  [:div {:class "columns px-3 pt-3"}
   [:div {:class "column is-narrow"}
    [left-drawer]]
   [:div {:class "column"}
    [tabs]
    [draw-plot]]])

(defn main-panel []
  (let [loading (rc/subscribe [:sub.ui/loading])]
    [:<>
     (if @loading
       [loading-panel]
       [main-view])]))
