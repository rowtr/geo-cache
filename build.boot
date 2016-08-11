(set-env!
  :dependencies  '[[adzerk/bootlaces            "0.1.11" :scope "test"]
                   [adzerk/env                  "0.2.0"]
                   [com.taoensso/carmine        "2.14.0"]]
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
