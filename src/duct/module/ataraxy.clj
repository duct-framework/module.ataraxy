(ns duct.module.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [duct.core :as duct]
            [duct.core.merge :as merge]
            [integrant.core :as ig]))

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

(def ^:private default-handlers
  {:ataraxy.error/unmatched-path   (ig/ref :duct.handler.static/not-found)
   :ataraxy.error/unmatched-method (ig/ref :duct.handler.static/method-not-allowed)
   :ataraxy.error/missing-params   (ig/ref :duct.handler.static/bad-request)
   :ataraxy.error/missing-destruct (ig/ref :duct.handler.static/bad-request)
   :ataraxy.error/failed-coercions (ig/ref :duct.handler.static/bad-request)
   :ataraxy.error/failed-spec      (ig/ref :duct.handler.static/bad-request)})

(defmethod ig/init-key :duct.module/ataraxy [_ routes]
  {:req #{:duct.core/project-ns
          :duct.handler.static/bad-request
          :duct.handler.static/not-found
          :duct.handler.static/method-not-allowed}
   :fn  (fn [config]
          (let [project-ns (:duct.core/project-ns config)
                handlers   (infer-handlers routes project-ns)
                middleware (infer-middleware routes project-ns)]
            (duct/merge-configs
             config
             {:duct.core/handler
              {:router (ig/ref :duct.router/ataraxy)}
              :duct.router/ataraxy
              {:routes     (with-meta routes {:demote true})
               :handlers   (with-meta (merge default-handlers handlers) {:demote true})
               :middleware (with-meta middleware {:demote true})}})))})
