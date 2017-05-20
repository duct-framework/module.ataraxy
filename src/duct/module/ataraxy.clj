(ns duct.module.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [duct.core :as duct]
            [duct.router.ataraxy :as router]
            [integrant.core :as ig]
            [medley.core :as m]))

(derive :duct.module/ataraxy :duct/module)

(defn- add-ns-prefix [kw prefix]
  (keyword (str prefix (if-let [ns (namespace kw)] (str "." ns)))
           (name kw)))

(defn- infer-keys [keys prefix]
  (into {} (for [k keys] [k (ig/ref (add-ns-prefix k prefix))])))

(defn- infer-handlers [routes project-ns]
  (infer-keys (ataraxy/result-keys routes) (str project-ns ".handler")))

(defn- middleware-keys [routes]
  (set (mapcat #(mapcat keys (:meta %)) (ataraxy/parse routes))))

(defn- infer-middleware [routes project-ns]
  (infer-keys (middleware-keys routes) (str project-ns ".middleware")))

(defmethod ig/init-key :duct.module/ataraxy [_ routes]
  {:req #{:duct.core/project-ns}
   :fn  (fn [config]
          (let [project-ns (:duct.core/project-ns config)]
            (duct/merge-configs
             {:duct.router/ataraxy
              {:routes     routes
               :handlers   (infer-handlers routes project-ns)
               :middleware (infer-middleware routes project-ns)}}
             config)))})
