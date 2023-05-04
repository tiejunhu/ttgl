(ns ttgl.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [ttgl.events :as events]
   [ttgl.subs]
   [ttgl.views :as views]
   [ttgl.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init []
  (events/initialize-db)
  (events/http-get-all)
  (dev-setup)
  (mount-root))
