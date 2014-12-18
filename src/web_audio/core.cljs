(ns web-audio.core
  "Low level wrapper around the objects in the WebAudio API.
  
  This name space provides functions for creating and manipulating the objects
  described in the WebAudio specificaion.
  
  See http://www.w3.org/TR/webaudio/")

;;;; Audio context

(defn create-context []
  "Creates and returns a WebAudio context"
  (let [constructor (or js/window.AudioContext
                        js/window.webkitAudioContext)]
    (constructor.)))

(defn current-time 
  "The current time in the AudioContext."
  [ctx] (.-currentTime ctx))

(defn sample-rate 
  "The sample rate of the AudioContext."
  [ctx] (.-sampleRate ctx))

(defn listener 
  "Gets the audio listner which is used for spatialization in the
  AudioContext."
  [ctx] (.-listener ctx))


;;;; Offline Audio Context

(defn create-offline-context []
  "OfflineAudioContext is a type of AudioContext for rendering or
  mixing-down faster than real-time."
  (js/OfflineAudioContext.))

(defn on-complete 
  "A completion funcion called when rendering has finished on an offline
  context."
  [offline-ctx] (.-oncomplete offline-ctx))

(defn start-rendering [offline-ctx] (.startRendering offline-ctx))


;;;; Audio nodes

;;; Constructors

(defn create-buffer-source            [ctx] (.createBufferSource ctx))
(defn create-media-stream-destination [ctx] (.createMediaStreamDestination ctx))
(defn create-analyser                 [ctx] (.createAnalyser ctx))
(defn create-gain                     [ctx] (.createGain ctx)) 
(defn create-biquad-filter            [ctx] (.createBiquadFilter ctx)) 
(defn create-wave-shaper              [ctx] (.createWaveShaper ctx)) 
(defn create-panner                   [ctx] (.createPanner ctx)) 
(defn create-convolver                [ctx] (.createConvolver ctx)) 
(defn create-dynamics-compressor      [ctx] (.createDynamicsCompressor ctx)) 


(defn create-media-element-source [ctx html-media-element] 
  (.createMediaElementSource ctx html-media-element)) 

(defn create-media-stream-source [ctx media-stream]
  (.createMediaStreamSource ctx media-stream))

(defn create-script-processor 
  ([ctx]                            (.createScriptProcessor ctx)) 
  ([ctx buffer-size]                (.createScriptProcessor ctx buffer-size)) 
  ([ctx buffer-size inputs]         (.createScriptProcessor ctx buffer-size inputs)) 
  ([ctx buffer-size inputs outputs] 
   (.createScriptProcessor ctx buffer-size inputs outputs)))

(defn create-delay 
  ([ctx]                (.createDelay ctx)) 
  ([ctx max-delay-time] (.createDelay ctx max-delay-time)))

(defn create-channel-splitter [ctx outputs]
  ([ctx]         (.createChannelSplitter ctx))
  ([ctx outputs] (.createChannelSplitter ctx outputs)))

(defn create-channel-merger [ctx inputs]
  ([ctx]        (.createChannelMerger ctx inputs))
  ([ctx inputs] (.createChannelMerger ctx inputs)))

;;; Protocols describing properties

(defprotocol ProcessesSignal
  "Functions applicable to everything that processes audio."

  (ctx-for [n] "Returns the audio context this is operating in."))

(extend-protocol ProcessesSignal
  js/AudioNode
  (ctx-for [node] (.-context node)))


(defprotocol OutputsSignal
  "Unifies operations on things that output audio."

  ; TODO: Allow users to specify output/input numbers
  (connect    [a b]  "Connects a to b.")
  (disconnect [a b]  "Disconnects a and b.")

  (connect-context-destination [n] "Connects node to its contexts destination"))

(extend-protocol OutputsSignal 
  js/AudioNode 
  (connect    [a b] (.connect a b))
  (disconnect [a b] (.disconnect a b))

  (connect-context-destination [n] (.connect n (.-destination (ctx-for n)))))


(defprotocol GeneratesSignal
  "Unifies operations on sound sources."

  (start [node] [node t] [node t offset] [node t offset duration]
    "Schedules sound or oscillator to commence playback at exact time.

    The t parameter describes at what time (in seconds) the sound should start
    playing. It is in the same time coordinate system as current-time.
    If 0 is passed in for this value or if the value is less than current-time,
    then the sound will start playing immediately.

    The offset parameter describes the offset time in the buffer (in seconds)
    where playback will begin. If 0 is passed in for this value, then playback
    will start from the beginning of the buffer.

    The duration parameter describes the duration of the portion (in seconds) to
    be played. If this parameter is not passed, the duration will be equal to
    the total duration of the AudioBuffer minus the offset parameter. Thus if
    neither offset nor duration are specified then the implied duration is the
    total duration of the AudioBuffer.")

  (stop [node] [node t]
    "Stops a sound source immediately or at time t if specified."))

(defn start
  ([node]                   (.start node 0))
  ([node t]                 (.start node t))
  ([node t offset]          (.start node t offset))
  ([node t offset duration] (.start node t offset duration)))

(defn stop 
  ([node]   (.stop node 0))
  ([node t] (.stop node t)))

(extend-protocol GeneratesSignal
  js/AudioBufferSourceNode
  start
  stop
  js/OscillatorNode 
  start
  stop)


(defprotocol Automatable
  "Unifies operations of automatable objects such as AudioParams."

  (set-value [param value]
    "Set's a paramater's value")

  (set-value-at [param value t] 
    "Schedules a param to change to value to at time t")

  (linear-ramp-to [param value t] 
    "Schedules a linear continuous change in parameter value from the previous
    scheduled parameter value to the given value at time t.")  

  (exponential-ramp-to [param value t]
    "Schedules an exponential change in parameter value from the previous
    scheduled parameter value to the given value at time t.")
  
  (set-target-at [param value start-time time-constant]
    "Exponentially approach the target with a rate having the given time
    constant.")
  
  (set-value-curve-at [param values start-time duration]
    "Sets a list of arbitrary parameter values starting at time for the given 
    duration. 

    The number of values will be scaled to fit into the desired duration.")
  
  (cancel-scheduled-events [param t]
    "Cancels scheduled values for paramater."))

(extend-protocol Automatable
  js/AudioParam
  (set-value               [p v]   (aset p "value" v))
  (set-value-at            [p v t] (.setValueAtTime p v t))
  (linear-ramp-to          [p v t] (.linearRampToValueAtTime p v t))
  (exponential-ramp-to     [p v t] (.exponentialRampToValueAtTime p v t))

  (set-target-at           [p v start-time time-constant] 
    (.setTargetAtTime p v start-time time-constant))

  (set-value-curve-at      [p vs start-time duration]
    (.setValueCurveATime p vs start-time duration))

  (cancel-scheduled-events [p t] (.cancelScheduledEvents p t)))


(defn create-oscillator               
  "OscillatorNode represents an audio source generating a periodic waveform. It
  can be set to a few commonly used waveforms. Additionally, it can be set to
  an arbitrary periodic waveform through the use of set-periodic-wave."
  [ctx] (.createOscillator ctx)) 

(defn type [osc] (.-type osc))


;;;; Buffers

(defn create-buffer [ctx channels length sample-rate]
  (.create-buffer ctx channels length sample-rate))

(defn decode-audio-data
  "Decodes audio data from an array buffer to something anaudio buffer can use
  and passes it on to success call back."
  ([ctx data success-callback] 
   (.decode-audio-data ctx data success-callback))

  ([ctx data success-callback error-callback] 
   (.decode-audio-data ctx data success-callback error-callback)))


;;;; Periodic Waves

(defn create-periodic-wave
  "Create a periodic wave."
  [ctx real imag] (.createPeriodicWave ctx real imag))

(defn set-periodic-wave
  "Sets the periodic wave used by an oscillator node."
  [osc wave] (.setPeriodicWave osc wave))
