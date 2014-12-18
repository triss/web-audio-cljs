(defproject web-audio "0.1.0-SNAPSHOT"
  :description "Low level WebAudio wrapper"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2496"]]
  
  :plugins [[lein-cljsbuild "1.0.3"]]

  :cljsbuild {:builds [;; build definition for the library
                       {:id "main" 
                        :source-paths ["src"] 
                        :jar true}
                       
                       ;; builds for the examples
                       {:id hello-world-example
                        :source-paths ["src" "examples/hello-world/src"]
                        :compiler {
                          :output-to     "examples/hello-world/main.js"
                          :output-dir    "examples/hello-world/out"
                          :source-map    true
                          :optimizations :none}}
                       
                       {:id sawtooth-example
                        :source-paths ["src" "examples/sawtooth/src"]
                        :compiler {
                          :output-to     "examples/sawtooth/main.js"
                          :output-dir    "examples/sawtooth/out"
                          :source-map    true
                          :optimizations :none}}]}

  :global-vars {*warn-on-reflection* true})
