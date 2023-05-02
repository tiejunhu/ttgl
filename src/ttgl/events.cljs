(ns ttgl.events
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [re-frame.core :as rc]
   [ttgl.db :as db]
   [ttgl.notion :as notion]))

(defn-traced initialize-db []
  (rc/dispatch-sync [:evt.db/initialize]))

(defn-traced http-get-all []
  (rc/dispatch [:evt.http/get-database])
  (rc/dispatch [:evt.http/list-all-items]))

(rc/reg-event-fx
 :evt.http/get-database
 (fn-traced [_ _]
            {:fx [[:http-xhrio (merge {:on-success [:evt.http/get-database-success]
                                       :on-failure [:evt.http/get-database-failure]}
                                      (notion/notion-get-database))]]}))

(rc/reg-event-fx
 :evt.http/list-all-items
 (fn-traced [_ _]
            {:fx [[:http-xhrio (merge {:on-success [:evt.http/get-items-success]
                                       :on-failure [:evt.http/get-items-failure]}
                                      (notion/notion-list-all))]]}))

(rc/reg-event-fx
 :evt.http/get-database-success
 (fn-traced [_ [_ database]]
            {:fx [[:dispatch [:evt.db/set-database (db/parse-incoming-database database)]]]}))

(rc/reg-event-fx
 :evt.http/get-items-success
 (fn-traced [_ [_ list]]
            {:fx [[:dispatch [:evt.db/set-items (db/parse-incoming-items list)]]]}))

(rc/reg-event-db
 :evt.db/initialize
 (fn-traced [_ _]
            db/default-db))

(rc/reg-event-db
 :evt.db/set-database
 (fn-traced [db [_ new-value]]
            (assoc db :database new-value)))

(rc/reg-event-db
 :evt.db/set-items
 (fn-traced [db [_ new-value]]
            (assoc db :items new-value)))
