(ns duct.module.ataraxy-test
  (:require [clojure.test :refer :all]
            [duct.core :as duct]
            [duct.module.ataraxy :as ataraxy]
            [integrant.core :as ig]))

(defn bar-handler [{[_ id] :ataraxy/result}]
  {:status 200, :headers {}, :body (str "bar " id)})

(defn wrap-quz [handler]
  #(assoc-in (handler %) [:headers "X-Quz"] "quz"))

(def config
  {:duct.core/project-ns 'foo
   :duct.module/ataraxy  '{["/bar" id] ^:quz [:bar id], ["/baz"] [:test/baz]}
   :foo.handler/bar      bar-handler
   :foo.middlware/quz    wrap-quz
   :duct.handler.static/not-found          {:status 404, :body "Not Found"}
   :duct.handler.static/method-not-allowed {:status 405, :body "Method Not Allowed"}
   :duct.handler.static/bad-request        {:status 400, :body "Bad Request"}})

(deftest module-test
  (is (= (duct/prep config)
         (merge config
                {:duct.core/handler
                 {:router (ig/ref :duct.router/ataraxy)}
                 :duct.router/ataraxy
                 {:routes
                  '{["/bar" id] ^:quz [:bar id], ["/baz"] [:test/baz]}
                  :handlers
                  {:bar      (ig/ref :foo.handler/bar)
                   :test/baz (ig/ref :foo.handler.test/baz)
                   :ataraxy.error/unmatched-path
                   (ig/ref :duct.handler.static/not-found)
                   :ataraxy.error/unmatched-method
                   (ig/ref :duct.handler.static/method-not-allowed)
                   :ataraxy.error/missing-params
                   (ig/ref :duct.handler.static/bad-request)
                   :ataraxy.error/missing-destruct
                   (ig/ref :duct.handler.static/bad-request)
                   :ataraxy.error/failed-coercions
                   (ig/ref :duct.handler.static/bad-request)}
                  :middleware
                  {:quz (ig/ref :foo.middleware/quz)}}}))))
