(ns geo-cache.mongo-cache
  (:require [geo-cache.cache :as cache :refer [get-cache shahash]])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.string :refer [trim]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern MongoClient])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))




(defonce cache (atom nil))

(defn cache-ready? []
  (let [icache @cache]
    (try
      (.getStats (:db icache))
      (catch Exception e
        (swap! cache merge (mg/connect-via-uri (:uri icache)))))
    (if (.getStats (:db @cache)) true false)))

(defn get-weight-from-cache [{:keys [from to]}]
  (when (cache-ready?)
    (let [hash (shahash from to)
          cache   @cache]
      (mc/find-one-as-map (:db cache) (:edge cache) {:id hash}))))

(defn add-weight-to-cache [from to distance duration points]
  (when (cache-ready?)
    (let [hash (shahash from to)
          cache   @cache]
      (mc/insert (:db cache) (:edge cache) {:id hash :distance distance :duration duration :points points :cache-date (Date.)})))
  (assoc {} :distance distance :duration duration))

(defn get-geocode-from-cache [address]
  (when (cache-ready?)
    (let [cache   @cache]
      (mc/find-one-as-map (:db cache) (:address cache) {:address (trim address)}))))

(defn add-geocode-to-cache [address lat lng]
  (when (cache-ready?)
    (let [cache    @cache]
      (mc/insert (:db cache) (:address cache) {:address address :lat lat :lng lng})))
  (assoc {} :address address :lat lat :lng lng))

(defrecord MongoCache []
  cache/IGeoCache
  (memoize-geocode [_ f] (cache/make-memoize-geocode {:infn f :addfn add-geocode-to-cache :getfn get-geocode-from-cache}))
  (memoize-weight  [_ f] (cache/make-memoize-weight {:infn f :addfn add-weight-to-cache :getfn get-weight-from-cache})))

(defmethod get-cache :mongo
  [{:keys [uri address edge] :as args}]
  (when (not (and uri address edge)) (throw (Exception. (str "Missing required arguments: uri: " uri " address: " address " edge: " edge))))
  (let [my-conn   (mg/connect-via-uri uri)]
    (reset! cache (merge my-conn {:uri uri :address address :edge edge}))
    (MongoCache.)))
