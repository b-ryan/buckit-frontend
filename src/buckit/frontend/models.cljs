(ns buckit.frontend.models
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn url [m] (str "http://localhost:8080/api/" (name m)))
