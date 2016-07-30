(set-env!
  :dependencies  '[[adzerk/bootlaces            "0.1.11" :scope "test"]
                   [adzerk/env                  "0.2.0"]
                   [com.novemberain/monger      "3.0.0-rc2"]
                   [com.taoensso/faraday        "1.8.0"]
                   [com.taoensso/carmine        "2.14.0"]
                   [com.datomic/datomic-free    "0.9.5153"] ]
  :target-path "target"
  :resource-paths #{"src"})
(require '[adzerk.bootlaces :refer :all])
(def +version+ "1.0.0-SNAPSHOT")
(task-options!
 pom  {:project     'rowtr/geo-cache
       :version     +version+
       :description "various cache mechanisms for geocodes and directions"
       :url         "https://github.com/rwillig/geo-cache"
       :scm         {:url "https://github.com/rwillig/geo-cache"}
       :license     {"MIT License" "http://opensource.org/licenses/mit-license.php"}}
 push {:repo        "clojars-upload" })
