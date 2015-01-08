(ns examples.mixing.core
  (:require [web-audio.core :refer [create-context
                                    create-oscillator create-gain    
                                    set-param! connect start
                                    connect-context-destination]]))

(def ctx (create-context))        

;;;; Create a couple osc's at simlar freqencies
(def osc-a (create-oscillator ctx))
(set-param! osc-a :frequency 440)

(def osc-b (create-oscillator ctx))
(set-param! osc-b :frequency 441)


;;;; Create a gain node to sum signals
(def osc-mixer (create-gain ctx))

;;; Connect up the oscilators to it
(connect osc-a osc-mixer)
(connect osc-b osc-mixer)

;;; Turn down the gain node to stop clipping.
(set-param! osc-mixer :gain 0.5)

;;; REVIEW: Should I impliment mix in this library?
#_(def osc-mixer (mix osc-a osc-b))


;;;; Route the audio out the speakers
(connect-context-destination osc-mixer)

;;;; Start both the oscillators
(start osc-a)                     
(start osc-b)
