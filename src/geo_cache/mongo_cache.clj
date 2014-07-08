(ns geo-cache.mongo-cache
  (:require [geo-cache.cache :as cache :refer [get-cache shahash]])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.string :refer [trim]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern MongoClient])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))


(defn- mongo-instance? []
  (instance? com.mongodb.MongoClient monger.core/*mongodb-connection*))

(defn- mongo-open? []
  (.. monger.core/*mongodb-connection* (getConnector) (isOpen)))


(defn- cache-ready? [the-cache]
  (let [conn  (if 
							  (and (mongo-instance?) (mongo-open?))
							  mg/*mongodb-connection* (mg/connect! (:uri the-cache)))]
    (mg/use-db! (:db the-cache))
    (if conn true false)))

(defn- get-weight-from-cache [cache from to]
  (let [hash (shahash from to)]
    (when (cache-ready? cache) (mc/find-one-as-map (:edge cache) { :id hash}))))

(defn- add-weight-to-cache [cache from to distance duration points]
  (let [hash (shahash from to)]
    (when (cache-ready? cache)
      (mc/insert (:edge cache) {:id hash :distance distance :duration duration :points points :cache-date (Date.)}))
    (assoc {} :distance distance :duration duration)))
  
(defn- get-geocode-from-cache [cache address]
  (when (cache-ready? cache) (mc/find-one-as-map (:address cache) {:address (trim address)})))

(defn- add-geocode-to-cache [cache address lat lng]
  (when (cache-ready? cache) (mc/insert (:address cache) {:address address :lat lat :lng lng}))
  (assoc {} :address address :lat lat :lng lng))

 
(defrecord MongoCache []
  cache/IGeoCache
  (memoize-geocode  [this f]
    (fn [addr]
      (if-let [e (get-geocode-from-cache this (:address addr))]
        (merge addr (select-keys e [:lat :lng]))
        (let [ret (f addr)]
          (add-geocode-to-cache this (:address ret) (:lat ret) (:lng ret))
          (merge addr ret)))))
  (memoize-weight   [this f]
    (fn [from to]
      (if-let [e (get-weight-from-cache this from to)]
        (select-keys e [:distance :duration :points])
        (let [ret (f from to)]
          (add-weight-to-cache this from to  (:distance ret) (:duration ret) (:points ret))
          ret))))
  
  )

(defmethod get-cache :mongo 
  [{:keys [uri db address edge] :as args}]
  (when (not (and uri db address edge)) (throw (Exception. (str "Missing required arguments: uri: " uri " db: " db " address: " address " edge: " edge))))
  (MongoCache. nil args))
 
