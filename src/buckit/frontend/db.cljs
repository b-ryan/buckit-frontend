(ns buckit.frontend.db
  (:require [buckit.frontend.http :as http]))

;TODO prismatic schema to define db?

(def initial-state
  {:url-path []
   :url-params {}

   http/accounts     {}
   http/payees       {}
   http/transactions {}

   :pending-initializations #{http/accounts http/payees http/transactions}})
