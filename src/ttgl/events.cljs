(ns ttgl.events
  (:require
   [day8.re-frame.http-fx]
   [re-frame.core :as rc]
   [ttgl.db :as db]
   [ttgl.notion :as notion]))

(defn initialize-db []
  (rc/dispatch-sync [:evt.db/initialize]))

(defn http-get-all []
  (rc/dispatch [:evt.http/get-database])
  (rc/dispatch [:evt.http/list-all-items]))

(rc/reg-event-fx
 :evt.http/get-database
 (fn [_ _]
   {:fx [[:http-xhrio (merge {:on-success [:evt.http/get-database-success]
                              :on-failure [:evt.http/get-database-failure]}
                             (notion/notion-get-database))]]}))

(rc/reg-event-fx
 :evt.http/list-all-items
 (fn [_ _]
   {:fx [[:http-xhrio (merge {:on-success [:evt.http/get-items-success]
                              :on-failure [:evt.http/get-items-failure]}
                             (notion/notion-list-all))]]}))

(rc/reg-event-fx
 :evt.http/get-database-success
 (fn [_ [_ database]]
   {:fx [[:dispatch [:evt.db/set-database (db/parse-incoming-database database)]]]}))

(rc/reg-event-fx
 :evt.http/get-items-success
 (fn [_ [_ list]]
   {:fx [[:dispatch [:evt.db/set-items (db/parse-incoming-items list)]]]}))

(rc/reg-event-db
 :evt.db/initialize
 (fn [_ _]
   db/default-db))

(rc/reg-event-db
 :evt.db/set-database
 (fn [db [_ new-value]]
   (assoc db :database new-value)))

(rc/reg-event-db
 :evt.db/set-items
 (fn [db [_ new-value]]
   (assoc db :items new-value)))

(rc/reg-event-db
 :evt.db/add-filter
 (fn [db [_ filter]]
   (update db :filters (fn [filters]
                         (assoc filters (keyword filter) true)))))

(rc/reg-event-db
 :evt.db/remove-filter
 (fn [db [_ filter]]
   (update db :filters (fn [filters]
                         (assoc filters (keyword filter) false)))))
