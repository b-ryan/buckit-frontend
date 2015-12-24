(ns buckit.frontend.backend
  (:require [buckit.frontend.models.core :as models]
            [buckit.frontend.utils       :as utils]
            [cljs-http.client            :as http]))

(def ^:private base-url "http://localhost:8080/api/")

(defn valid-method?
  [method]
  (contains? #{:get-many :get-one :post :put :save} method))

(defn returns-many?
  "The types of methods that will contain a list of items in the response."
  [method]
  {:pre [(valid-method? method)]}
  (contains? #{:get-many} method))

(def returns-one? (complement returns-many?))

(defn- q-filter->query-param
  "A \"q\" filter is named such due to the use of Flask-Restless in the
  backend. See
  https://flask-restless.readthedocs.org/en/latest/searchformat.html#query-format"
  [q-filter]
  (if q-filter
    {:q (.stringify js/JSON (clj->js q-filter))}
    {}))

(defn get-many
  [resource & [q-filter]]
  (http/get (str base-url (name resource))
            {:with-credentials? false
             :query-params (q-filter->query-param q-filter)}))

(defn get-one
  [resource id]
  (http/get (str base-url (name resource) "/" id)
            {:with-credentials? false}))

(defn post
  [resource body]
  {:pre [(some? body) (nil? (models/id body))]}
  (http/post (str base-url (name resource))
             {:with-credentials? false
              :json-params body}))

(defn put
  [resource body]
  {:pre [(some? body) (some? (models/id body))]}
  (http/put (str base-url (name resource) "/" (models/id body))
            {:with-credentials? false
             :json-params body}))

(defn save
  [resource body]
  (let [method (if (models/id body) put post)]
    (method resource body)))

(defmulti request (fn [method & _] method))

(defmethod request :get-many
  [_ & args]
  (apply get-many args))

(defmethod request :get-one
  [_ & args]
  (apply get-one args))

(defmethod request :post
  [_ & args]
  (apply post args))

(defmethod request :put
  [_ & args]
  (apply put args))

(defmethod request :save
  [_ & args]
  (apply save args))
