(ns buckit.frontend.http
  (:require [cljs-http.client :as http]))

(def accounts :accounts)
(def payees :payees)
(def transactions :transactions)

(def ^:private base-url "http://localhost:8080/api/")

(defn get-many
  [resource]
  (http/get (str base-url (name resource)) {:with-credentials? false}))

(defn get-one
  [resource id]
  (http/get (str base-url (name resource) "/" id) {:with-credentials? false}))

(defn post
  [resource id body]
  (http/post (str base-url (name resource)) {:with-credentials? false
                                             :json-params body}))
(defn put
  [resource id body]
  (http/put (str base-url (name resource) "/" id) {:with-credentials? false
                                                   :json-params body}))
