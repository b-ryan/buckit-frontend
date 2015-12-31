(ns buckit.frontend.utils)

(defn index-by-key
  "Like group-by, but the values are just one object. If there are any
  duplicates for key k, only one will be used."
  [k objs]
  (zipmap (map k objs) objs))

(defn filter-map-by-v
  "Filters a map and returns a map. Only the values will be passed into the
  filter function."
  [m f]
  {:pre [(fn? f) (map? m)]}
  (into {} (filter (fn [[k v]] (f v)) m)))

(defn spy
  [x]
  (js/console.log "spy results:" (clj->js x))
  x)

(defn nil-or-?
  [x what?]
  (or (nil? x) (what? x)))

(defn nil-or-integer?
  [x]
  (nil-or-? x integer?))

(defn nil-or-boolean?
  [x]
  (contains? #{nil true false} x))
