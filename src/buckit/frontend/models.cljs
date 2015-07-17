(ns ^:figwheel-always buckit.frontend.models
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn url [m] (str "http://localhost:8080/api/" m))
