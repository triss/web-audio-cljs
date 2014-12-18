(ns examples.sawtooth.core
  (:require [web-audio.core :as wa]))

(def ctx (wa/create-context))        

(def osc (wa/create-oscillator ctx))
(wa/set! osc :type "sawtooth")

(wa/connect-context-destination osc)
(wa/start osc)                     
