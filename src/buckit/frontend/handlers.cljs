(ns buckit.frontend.handlers
  (:require [re-frame.core :refer [register-handler path]]
            [buckit.frontend.db :refer [default-db]]))

(register-handler
  :initialize-db
  (fn [db _]
    default-db))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc :url-path url-path)
        (assoc :url-params url-params))))
