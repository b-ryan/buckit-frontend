(ns buckit.frontend.db.query)

(def status :status)

(def complete-status :complete) ; FIXME successful
(def error-status    :error) ; FIXME failed
(def pending-status  :pending)

(defn successful?
  [result]
  (= complete-status (status result)))

(defn failed?
  [result]
  (= error-status (status result)))

(defn pending?
  [result]
  (= pending-status (status result)))

(defn complete?
  [result]
  (or (successful? result) (failed? result)))
