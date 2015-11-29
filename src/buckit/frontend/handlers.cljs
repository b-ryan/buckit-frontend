(ns buckit.frontend.handlers
  (:require [buckit.frontend.db :as buckit.db]
            [re-frame.core :refer [register-handler path]]))

(register-handler
  :initialize-db
  (fn [db _]
    buckit.db/default-db))

(register-handler
  :url-changed
  (fn [db [_ url-path url-params]]
    (-> db
        (assoc :url-path url-path)
        (assoc :url-params url-params))))
