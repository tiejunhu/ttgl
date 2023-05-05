(ns ttgl.views
  (:require
   [re-frame.core :as rc]
   [ttgl.plot :as plot]))

(def ^:private left-drawer-width 180)

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
    [:<> [:div {:class "card mb-4" :style {:width (str left-drawer-width "px")}}
          [:div {:class "card-content"}
           [:div {:class "content is-size-7"}
            "Data are collected from " [:a {:href "https://ttgearlab.com/category/report/" :target "_blank"} "TTGEARLAB's reports"] ", all the credit goes to " [:a {:href "https://ttgearlab.com/" :target "_blank"} "TTGEARLAB"] "."]]]
     [:aside {:class "menu" :style {:width (str left-drawer-width "px")}}
      [:p {:class "menu-label"} "Brands"]
      (left-drawer-constructs :sub.data/sorted-brands @filters)
      [:p {:class "menu-label"} "Construction"]
      (left-drawer-constructs :sub.data/sorted-construct @filters)
      [:p {:class "menu-label"} "Fiber Type"]
      (left-drawer-constructs :sub.data/sorted-fiber-type @filters)]
     [:div {:class "mt-4 is-flex is-justify-content-center"}
      [:iframe {:src "https://ghbtns.com/github-btn.html?user=tiejunhu&repo=ttgl&type=star&count=true" :frameborder "0" :scrolling "0" :width "70" :height "20" :title "GitHub"}]]]))

(defn- loading-panel []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :justify-content "center"
                 :align-items "center"
                 :text-align "center"
                 :min-height "100vh"}}
   [:progress {:class "progress is-large is-info" :max "100" :style {:width "60%"}} "60%"]
   [:h4 {:class "subtitle is-4"} "Loading ..."]])


(defn- draw-index-plot [plot-name props]
  (let [items (rc/subscribe [:sub.data/items-data])
        x-field (:x-field props)
        y-field (:y-field props)
        size-field (:size-field props)
        tooltip-fields (:tooltip-fields props)]
    [plot/component plot-name {:data @items
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
                               :annotations (plot/annotations props)
                               :xAxis (plot/x-axis props)
                               :yAxis (plot/y-axis props)
                               :meta plot/plot-meta}]))

(defn- draw-plot []
  (let [active-tab (rc/subscribe [:sub.ui/active-tab])]
    (cond
      (= @active-tab :ecp) (draw-index-plot "ecp-plot"
                                            {:x-field "ep"
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
      (= @active-tab :vlp) (draw-index-plot "vlp-plot"
                                            {:x-field "vp"
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
   [:div {:class "column is-narrow pr-0"}
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
