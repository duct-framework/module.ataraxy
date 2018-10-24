(ns duct.module.ataraxy-test
  (:require [clojure.test :refer :all]
            [duct.core :as core]
            [duct.module.ataraxy :as ataraxy]
            [integrant.core :as ig]))

(core/load-hierarchy)

(defn bar-handler [{[_ id] :ataraxy/result}]
  {:status 200, :headers {}, :body (str "bar " id)})

(defn not-found-handler [_]
  {:status 404, :headers {}, :body "Not found"})

(defn wrap-quz [handler]
  #(assoc-in (handler %) [:headers "X-Quz"] "quz"))

(def basic-config
  {:duct.module/ataraxy
   '{["/bar" id] ^:quz [:bar id]
     ["/baz"]    [:test/baz]}
   :duct.profile/base
   {:duct.core/project-ns 'foo
    :foo.handler/bar      bar-handler
    :foo.middlware/quz    wrap-quz
    :duct.handler.static/not-found          {:status 404, :body "Not Found"}
    :duct.handler.static/method-not-allowed {:status 405, :body "Method Not Allowed"}
    :duct.handler.static/bad-request        {:status 400, :body "Bad Request"}}})

(def updated-handlers-config
  (-> basic-config
      (assoc-in [:duct.profile/base :foo.handler/not-found] not-found-handler)
      (assoc-in [:duct.profile/base :duct.router/ataraxy]
                {:handlers {:ataraxy.error/unmatched-path
                            (ig/ref :foo.handler/not-found)}})))

(deftest module-test
  (testing "basic config"
    (is (= (merge (:duct.profile/base basic-config)
                  {:duct.handler/root
                   {:router (ig/ref :duct.router/ataraxy)}
                   :duct.router/ataraxy
                   {:routes
                    '{["/bar" id] ^:quz [:bar id]
                      ["/baz"]    [:test/baz]}
                    :handlers
                    {:bar      (ig/ref :foo.handler/bar)
                     :test/baz (ig/ref :foo.handler.test/baz)}
                    :middleware
                    {:quz (ig/ref :foo.middleware/quz)}}})
           (core/build-config basic-config))))

  (testing "updated handlers"
    (is (= (merge (:duct.profile/base updated-handlers-config)
                  {:duct.handler/root
                   {:router (ig/ref :duct.router/ataraxy)}
                   :duct.router/ataraxy
                   {:routes
                    '{["/bar" id] ^:quz [:bar id]
                      ["/baz"]    [:test/baz]}
                    :handlers
                    {:bar      (ig/ref :foo.handler/bar)
                     :test/baz (ig/ref :foo.handler.test/baz)
                     :ataraxy.error/unmatched-path (ig/ref :foo.handler/not-found)}
                    :middleware
                    {:quz (ig/ref :foo.middleware/quz)}}})
           (core/build-config updated-handlers-config)))))
