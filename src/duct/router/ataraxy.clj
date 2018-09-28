(ns duct.router.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as resp]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [duct.core.resource]
            [integrant.core :as ig]))

(def ^:private default-handlers
  {:ataraxy.error/unmatched-path   (ig/ref :duct.handler.static/not-found)
   :ataraxy.error/unmatched-method (ig/ref :duct.handler.static/method-not-allowed)
   :ataraxy.error/missing-params   (ig/ref :duct.handler.static/bad-request)
   :ataraxy.error/missing-destruct (ig/ref :duct.handler.static/bad-request)
   :ataraxy.error/failed-coercions (ig/ref :duct.handler.static/bad-request)
   :ataraxy.error/failed-spec      (ig/ref :duct.handler.static/bad-request)})

(extend-protocol resp/ToResponse
  duct.core.resource.Resource
  (->response [r] (resp/->response (io/as-url r))))

(defn- missing-handlers [{:keys [routes handlers]}]
  (set/difference (set (ataraxy/result-keys routes)) (set (keys handlers))))

(defn- add-inferred-handlers [handler-map keys-to-add]
  (reduce (fn [m k] (assoc m k (ig/ref k))) handler-map keys-to-add))

(defmethod ig/prep-key :duct.router/ataraxy [_ options]
  (-> options
      (update :handlers #(merge default-handlers %))
      (update :handlers add-inferred-handlers (missing-handlers options))))

(defmethod ig/init-key :duct.router/ataraxy [_ options]
  (ataraxy/handler options))
