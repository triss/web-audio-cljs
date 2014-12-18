(ns examples.sawtooth.core
  (:require [web-audio.core :as wa]))

(def ctx (wa/create-context))        

(def osc (wa/create-oscillator ctx))
(set! (.-type osc) "sawtooth")

(wa/connect-context-destination osc)
(wa/start osc)                     
