(ns buckit.frontend.db
  (:require [buckit.frontend.http :as http]
            [buckit.frontend.models.core :as models]
            [buckit.frontend.utils :as utils]))

;TODO prismatic schema to define db?

(def initial-state
  {:url-path   []
   :url-params {}

   :resources {http/accounts     {}
               http/payees       {}
               http/transactions {}}

   :pending-initializations #{http/accounts http/payees}

   ; queries represent GET requests to the backend database. They are a
   ; combination of resource type and optional ID and query parameters.
   :queries {}

   })

(def url-path   :url-path)
(def url-params :url-params)

(def pending-initializations :pending-initializations)

(defn get-resource
  [db resource]
  (get-in db [:resources resource]))

(defn inject-resources
  [db resource objs]
  (js/console.log (str (count objs)
                       " object(s) being added to resource "
                       resource))
  (update-in db [:resources resource] merge (utils/index-by-key models/id objs)))

(defn complete-initialization
  [db resource]
  (update-in db [:pending-initializations] disj resource))

(defn update-query
  [db query m]
  (assoc-in db [:queries query] m))
