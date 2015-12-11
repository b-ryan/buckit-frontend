(ns buckit.frontend.db
  (:require [buckit.frontend.http :as http]))

;TODO prismatic schema to define db?

(def initial-state
  {:url-path []
   :url-params {}

   :accounts {}
   :payees {}
   :transactions {}

   ; some sort of information about what is loading
   })
