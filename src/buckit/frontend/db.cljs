(ns buckit.frontend.db
  (:require [buckit.frontend.models.core        :as models]
            [buckit.frontend.models.transaction :as models.transaction]
            [buckit.frontend.utils              :as utils]))

;TODO prismatic schema to define db?

(def url-path                :url-path)
(def url-params              :url-params)
(def resources               :resources)

; queries represent GET requests to the backend database. They are a
; combination of resource type and optional ID and query parameters.
(def queries                 :queries)


(def initial-state
  {url-path                []
   url-params              {}
   resources               {}
   queries                 {}})


(defn get-resources
  [db model-type]
  {:pre [(models/valid-model-type? model-type)]}
  (get-in db [resources model-type] {}))

(defn- add-resources-to-db
  "Helper function for inject-resources. This is what actually puts the models
  into the database."
  [db model-type objs]
  {:pre [(models/valid-model-type? model-type)
         (sequential? objs)]}
  (js/console.log (str (count objs) " object(s) being added to resource "
                       model-type))
  (update-in db [resources model-type]
             merge (utils/index-by-key models/id objs)))

(defmulti inject-resources
  "Puts the given resources into the database. Resources are things like
  transactions, payees, and accounts. Normally these resources are saved into
  a map within the db where the key is the resource's ID and the value is the
  resource itself. Doing so makes writing into the db and reading out of it
  very easy and fast. We want to avoid having to search a list to overwrite
  resources that have been re-loaded.

  In many cases, a given resource may have a resource of another type nested
  inside it. For example, a transaction will come back from the backend
  likeso:

  {:id       100
   :payee_id 22
   :payee    {:id   22
              :name \"foo\"}}

  When this happens, inject-resources is called again with those nested
  resources. This is done in case a PUT or POST request changes or creates a
  nested object. For example, the backend will allow you to transactionally
  save resources. You might PUT to /transactions, but the transaction contains
  a nested :payee which refers to a new Payee object. The backend will save the
  new Payee and the transaction itself."
  (fn [db model-type objs] model-type))

(defmethod inject-resources :default
  [db model-type objs]
  (add-resources-to-db db model-type objs))

(defmethod inject-resources models/transactions
  [db model-type transactions]
  (let [payees (filter identity (map models.transaction/payee transactions))]
    (-> db
        (add-resources-to-db model-type transactions)
        (inject-resources models/payees payees))))

(defn update-query
  [db query-id f & args]
  (update-in db [queries query-id] #(apply f % args)))
