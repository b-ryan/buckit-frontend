(ns buckit.frontend.http
  (:require [cljs-http.client :as http]))

(def accounts :accounts)
(def payees :payees)
(def transactions :transactions)

(defn url
  [resource]
  (str "http://localhost:8080/api/" (name resource)))

(defn query
  [resource]
  (http/get (url resource) {:with-credentials? false}))
