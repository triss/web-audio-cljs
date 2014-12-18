(ns examples.hello-world.core
  (:require [web-audio.core :as wa]))

(def ctx (wa/create-context))        ; create a WebAudio context,
(def osc (wa/create-oscillator ctx)) ; create an oscillator in it,
(wa/connect-context-destination osc) ; connect osc to context dest. (speakers),
(wa/start osc)                       ; and start the oscillaor playing
