(ns geo-cache.redis-cache
  (:require [geo-cache.cache :as cache :refer [get-cache shahash]])
  (:require [taoensso.carmine :as car :refer [wcar]]
            [clojure.string :refer [trim]])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))


(defn conn-opts [](assoc 
                      {} 
                      :pool {} 
                      :spec 
                      (into {} (filter #(identity (val %))
                        {:host (System/getProperty "REDIS_HOST")
                         :port (try (java.lang.Integer/parseInt (System/getProperty "REDIS_PORT"))
                                    (catch Exception e nil))}))))

(defmacro wcar* [& body] `(wcar (~conn-opts) ~@body))

(defn add-geocode [address lat lng]
  (let [data      (assoc {} :lat lat :lng lng)]
    (wcar* (car/set  (str "address:" address) data))
  data))

(defn get-geocode [address]
  (wcar*  (car/get  (str "address:" address))))

(defn get-weight [from to]
  (let [hash (shahash from to)]
    (wcar* (car/get (str "edge:" hash)))))

(defn add-weight [from to distance duration points]
  (let [hash                      (shahash from to)
        data                      (assoc 
                                    {}
                                    :distance distance
                                    :duration duration
                                    :points   points
                                    :cache-date (Date.))]
    (wcar*  (car/set (str "edge:" hash) data)))
  (assoc {} :distance distance :duration duration))

(defrecord RedisCache
  []
  cache/IGeoCache
  (memoize-geocode  [_ f]
    (cache/make-memoize-geocode {:infn f :addfn add-geocode :getfn get-geocode}))
  (memoize-weight   [_ f]
    (cache/make-memoize-weight {:infn f :addfn add-weight :getfn get-weight})))

(defmethod get-cache :redis
 [_] 
 (RedisCache.))
