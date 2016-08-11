(ns co.rowtr.geo-cache.cache
  (:import java.util.UUID))

(defprotocol IGeoCache
  (memoize-geocode  [this f])
  (memoize-weight   [this f]))

(defmacro make-memoize-geocode [{:keys  [infn getfn addfn]}]
    `(fn [addr#]
      (if-let [e# (~getfn (:address addr#))]
        (merge addr# (select-keys e# [:lat :lng]))
        (let [ret# (~infn addr#)]
          (~addfn (:address ret#) ret#)
          (merge addr# ret#)))))

(defmacro make-memoize-weight [{:keys  [infn getfn addfn]}]
  `(fn [rec#]
      (if-let [e# (~getfn rec# )]
        (select-keys e# [:distance :duration :points])
        (let [ret# (~infn rec#)]
          (when-not (-> ret# meta :no-cache)
            (~addfn (:from rec#) (:to rec#)  ret#))
          ret#))))

(defmulti get-cache :type)

(def ^:dynamic *cache* nil)

(defn coords->str
  [location]
  (str (:lat location) "," (:lng location)))

(defn sha1 [s]
     (->> (-> "sha1"
              java.security.MessageDigest/getInstance
              (.digest (.getBytes s)))
          (map #(.substring
                 (Integer/toString
                  (+ (bit-and % 0xff) 0x100) 16) 1))
          (apply str)))

(defn shahash [from to]
  (sha1 (str (sha1 (coords->str from))(coords->str to))))






