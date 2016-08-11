(ns co.rowtr.geo-cache.redis-cache
  (:require
    [adzerk.env      :as env]
    [co.rowtr.geo-cache.cache :as cache :refer [get-cache shahash]])
  (:require [taoensso.carmine :as car :refer [wcar]]
            [clojure.string :refer [trim]])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))

(env/def
  REDIS_URI   nil)

(defn conn-opts [](assoc
                      {}
                      :pool {}
                      :spec {:uri REDIS_URI}))

(defmacro wcar* [& body] `(wcar (~conn-opts) ~@body))

(defn add-geocode [address rec]
  (wcar* (car/set  (str "address:" address) rec))
  rec)

(defn get-geocode [address]
  (wcar*  (car/get  (str "address:" address))))

(defn get-weight [{:keys [from to]}]
  (let [hash (shahash from to)]
    (wcar* (car/get (str "edge:" hash)))))

(defn add-weight [from to rec]
  (let [hash                      (shahash from to)
        data                      (assoc
                                    rec
                                    :cache-date (Date.))]
    (wcar*  (car/set (str "edge:" hash) data)))
  rec)

(defrecord RedisCache
  [conn]
  cache/IGeoCache
  (memoize-geocode  [_ f]
    (cache/make-memoize-geocode {:infn f :addfn add-geocode :getfn get-geocode}))
  (memoize-weight   [_ f]
    (cache/make-memoize-weight {:infn f :addfn add-weight :getfn get-weight})))

(defmethod get-cache :redis
 [_]
 (RedisCache. (conn-opts)))
