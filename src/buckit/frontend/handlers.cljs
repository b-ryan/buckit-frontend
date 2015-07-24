(ns buckit.frontend.handlers
  (:require [re-frame.core :refer [register-handler path]]
            [buckit.frontend.db :refer [default-db]]))

(register-handler
  :initialize-db
  (fn [db _]
    default-db))

(register-handler
  :activate-section
  (fn [db [_ value]]
    (assoc db :active-section value)))
