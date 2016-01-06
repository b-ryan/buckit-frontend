(ns buckit.frontend.utils)

(defn index-by-key
  "Like group-by, but the values are just one object. If there are any
  duplicates for key k, only one will be used."
  [k objs]
  (zipmap (map k objs) objs))

(defn- map-comp
  [comp-f m user-f]
  {:pre [(fn? comp-f) (map? m) (fn? user-f)]}
  (into {} (comp-f (fn [[k v]] (user-f v)) m)))

(def filter-map-by-v
  "Filters a map and returns a map. Only the values will be passed into the
  filter function."
  (partial map-comp filter))

(def remove-map-by-v
  "Filters a map and returns a map. Only the values will be passed into the
  remote function."
  (partial map-comp remove))

(defn spy
  ([x]
   (spy "?" x))
  ([desc x]
   (js/console.log (str "spy results [" desc "] : ") (clj->js x))
   x))

(defn either-fn
  [a-fn b-fn]
  (fn [& args]
    (or (apply a-fn args)
        (apply b-fn args))))

(defn nil-or-?
  [x what?]
  (or (nil? x) (what? x)))

(defn nil-or-integer?
  [x]
  (nil-or-? x integer?))

(defn nil-or-boolean?
  [x]
  (contains? #{nil true false} x))
