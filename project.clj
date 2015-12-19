(defproject buckit/frontend "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  ; https://github.com/bhauman/lein-figwheel/issues/285
  :min-lein-version "2.5.3"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cljs-http "0.1.35"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [re-frame "0.4.1"]
                 [reagent "0.5.0"]
                 [secretary "1.2.3"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-doo "0.1.4"]
            [lein-figwheel "0.5.0-2" :exclusions [org.clojure/tools.reader
                                                  ring/ring-core]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [; "dev" build outputs JS for development, complete with
               ; auto-reloading using figwheel.
               {:id "dev"
                :source-paths ["src" "env/dev/src"]

                :figwheel { :on-jsload "buckit.frontend.dev/on-js-reload" }

                :compiler {:main buckit.frontend.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/buckit_frontend.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true }}

               ; "min" build produces minified JS using "advanced" optimizations.
               ; It is not currently ready for production.
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/buckit_frontend.js"
                           :main buckit.frontend.core
                           :optimizations :advanced
                           :pretty-print false}}

               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "resources/public/js/testable.js"
                           :main 'buckit.frontend.runner
                           :optimizations :none}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources" 
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1" 

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 3450

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
             })
