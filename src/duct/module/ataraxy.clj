(ns duct.module.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.error :as err]
            [duct.core :as duct]
            [duct.core.merge :as merge]
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

(def ^:private default-handlers
  {::err/unmatched-path   (ig/ref :duct.handler.error/not-found)
   ::err/unmatched-method (ig/ref :duct.handler.error/method-not-allowed)
   ::err/missing-params   (ig/ref :duct.handler.error/bad-request)
   ::err/missing-destruct (ig/ref :duct.handler.error/bad-request)
   ::err/failed-coercions (ig/ref :duct.handler.error/bad-request)})

(defmethod ig/init-key :duct.module/ataraxy [_ routes]
  {:req #{:duct.core/project-ns
          :duct.handler.error/bad-request
          :duct.handler.error/not-found
          :duct.handler.error/method-not-allowed}
   :fn  (fn [config]
          (let [project-ns (:duct.core/project-ns config)
                handlers   (infer-handlers routes project-ns)
                middleware (infer-middleware routes project-ns)]
            (duct/merge-configs
             config
             {:duct.core/handler
              {:router (ig/ref :duct.router/ataraxy)}
              :duct.router/ataraxy
              {:routes     (merge/displace routes)
               :handlers   (merge/displace (merge default-handlers handlers))
               :middleware (merge/displace middleware)}})))})
