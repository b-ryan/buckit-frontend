(ns buckit.frontend.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub
  :url-path
  (fn [db _]
    (reaction (:url-path @db))))

(register-sub
  :accounts
  (fn [db _]
    (reaction (:accounts @db))))
