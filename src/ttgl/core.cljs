(ns ttgl.core
  (:require
   ;; [reagent.dom :as rdom]
   [reagent.dom.client :as rclient]
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
  (let [root-el (.getElementById js/document "app")
        root (rclient/create-root root-el)]
    (rclient/render root [views/main-panel])))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init []
  (events/initialize-db)
  (events/http-get-all)
  (dev-setup)
  (mount-root))
