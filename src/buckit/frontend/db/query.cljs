(ns buckit.frontend.db.query
  (:require [cljs-http.client :as http]))

(defn create-pending
  [query-id]
  {:query-id query-id
   :status   :pending
   :response nil})

(defn set-complete
  [q response]
  (assoc q
         :status   :complete
         :response response))

(defn pending?
  [q]
  (= :pending (:status q)))

(defn complete?
  [q]
  (= :complete (:status q)))

(defn successful?
  [q]
  (and (complete? q)
       (:success (:response q))))

(defn failed?
  [q]
  (and (complete? q)
       (not (successful? q))))
