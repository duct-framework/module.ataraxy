(ns duct.module.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as resp]
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
  {::resp/bad-request           (ig/ref :duct.handler.error/bad-request)
   ::resp/not-found             (ig/ref :duct.handler.error/not-found)
   ::resp/method-not-allowed    (ig/ref :duct.handler.error/method-not-allowed)
   ::resp/internal-server-error (ig/ref :duct.handler.error/internal-error)})

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
