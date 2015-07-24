(ns buckit.frontend.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub
  :active-section
  (fn [db _]
    (reaction (:active-section @db))))
