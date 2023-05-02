(ns ttgl.notion
  (:require [ajax.core :as ajax]))

(def notion-token "secret_GEl42ZcQ4XfzKxFQIV9oedhuqHYXRLFaPc9hewMVDs7")
(def gear-lab-database "16c5a47f685243a39e2153c6a0f04cc4")

(defn- notion-url [appendix]
  (str "https://b.oldhu.com/notion-api/v1/" appendix))

(defn- notion-database-url [database]
  (notion-url (str "databases/" database)))

(defn- notion-request [method url token query]
  (merge query {:method method
                :uri url
                :response-format (ajax/json-response-format {:keywords? true})
                :headers {:Authorization (str "Bearer " token)
                          :Notion-Version "2022-06-28"}}))

(defn- notion-get-database' [database token]
  (notion-request :get (notion-database-url database) token {}))

(defn notion-get-database []
  (notion-get-database' gear-lab-database notion-token))

(defn- notion-list-all' [database token]
  (notion-request :post (notion-database-url (str database "/query")) token
                  {:params {:sorts [{:timestamp "last_edited_time"
                                     :direction "descending"}]
                            :page_size 2000}
                   :format (ajax/json-request-format)}))

(defn notion-list-all []
  (notion-list-all' gear-lab-database notion-token))
