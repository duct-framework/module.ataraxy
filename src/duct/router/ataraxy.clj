(ns duct.router.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as resp]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [duct.core.resource]
            [integrant.core :as ig]))

(extend-protocol resp/ToResponse
  duct.core.resource.Resource
  (->response [r] (resp/->response (io/as-url r))))

(defn- missing-handlers [{:keys [routes handlers]}]
  (set/difference (set (ataraxy/result-keys routes)) (set (keys handlers))))

(defn- add-default-handlers [handler-map keys-to-add]
  (reduce (fn [m k] (assoc m k (ig/ref k))) handler-map keys-to-add))

(defmethod ig/prep-key :duct.router/ataraxy [_ options]
  (update options :handlers add-default-handlers (missing-handlers options)))

(defmethod ig/init-key :duct.router/ataraxy [_ options]
  (ataraxy/handler options))
