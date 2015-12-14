(ns buckit.frontend.models.transaction
  (:require [buckit.frontend.models.split :as models.split]
            [buckit.frontend.utils        :as utils]
            [cljs-time.coerce             :as time.coerce]
            [cljs-time.core               :as time.core]
            [cljs-time.format             :as time.fmt]))

(def id :id)
(def date :date)
(def payee-id :payee_id)
(def splits :splits)

(defn create
  "Creates a transaction with 2 splits. If an account ID is provided, it will
  be used as the account ID of the first split. The date will be today."
  [& [account-id]]
  {id       nil
   ;TODO do not format the date, just pass it along.
   ; consider using built-in :year-month-day formatter
   date     (time.fmt/unparse (time.fmt/formatter "yyyy-MM-dd")
                              (time.coerce/to-date-time (time.core/today)))
   payee-id nil
   splits   [(utils/spy (models.split/create account-id)) (models.split/create)]})
