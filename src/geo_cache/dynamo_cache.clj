(ns geo-cache.dynamo-cache
  (:require [geo-cache.cache :as cache :refer [get-cache shahash]])
  (:require [taoensso.faraday :as far]
            [clojure.string :refer [trim]])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date))


(defn add-geocode-to-cache [cache address lat lng]
  (let [data      (assoc {} :address address :lat lat :lng lng)]
    (far/put-item (:creds cache) (:address cache) data)
  data))

(defn get-geocode-from-cache [cache address]
  (far/get-item (:creds cache) (:address cache) {:address address}))

(defn get-weight-from-cache [cache from to]
  (let [hash (shahash from to)]
    (far/get-item (:creds cache) (:edge cache) {:id hash})))

(defn add-weight-to-cache [cache from to distance duration points]
  (let [hash (shahash from to)]
    (far/put-item (:creds cache) (:edge cache) {:id hash :distance distance :duration duration :points points :cache-date (Date.)}))
  (assoc {} :distance distance :duration duration))
 
(defrecord DynamoCache []
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
          ret)))))

(defmethod get-cache :dynamo
  [{:keys [access secret address edge] :as args}]
  (when (not (and address edge)) (throw (Exception. (str "Missing required arguments: " address " " edge ))))
  (let [creds       (assoc {} :access-key access :secret-key secret)]
    (DynamoCache. nil (assoc {} :creds creds :address address :edge edge))))
