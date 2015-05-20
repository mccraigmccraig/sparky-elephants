(ns sparky-elephants.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]

            [cljs.core.async :as async :refer [<! >! put! close!]]
            [chord.client :refer [ws-ch]]
            [cljs-uuid-utils.core :as uuid])
  (:import goog.History))

(defonce client-uuid (uuid/uuid-string (uuid/make-random-uuid)))

(defonce user-id-value (atom nil))
(defonce mailbox-chan (atom nil))
(defonce message-history-value (atom nil))

(defn log-channel
  "logs messages from the websocket channel to the atom log"
  [ch log]
  (.log js/console "LOGGING")
  (go
    (while (let [{:keys [message error] :as r} (<! ch)]
             ;; (.log js/console (clj->js [r message error]))
             (swap! log conj message)
             r))
    (.log js/console "FINISHED")))

(defn open-ws
  "returns a single-value channel containing the websocket channel on (or false)"
  [url]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch url {:format :transit-json}))]
      (if-not error
        ws-channel
        (do (js/console.log "Error:" (pr-str error))
            false)))))

(defn set-user
  [user-id]
  (let [user-id (not-empty user-id)
        host (-> js/window.location .-host)]
    (go
      (reset! user-id-value user-id)
      (swap! mailbox-chan (fn [old-chan] (when old-chan (close! old-chan)) nil))
      (reset! message-history-value nil)
      (when user-id
        (when-let [ch (<! (open-ws (str "ws://" host "/mailbox/" user-id "?client-uuid=" client-uuid)))]
          (reset! mailbox-chan ch)
          (log-channel ch message-history-value))))))

(defn send-message
  [message]
  (js/console.log message)
  (when-let [sock @mailbox-chan]
    (put! sock message)))

;; -------------------------
;; Views

(def message-form-template
  [:div
   [:div.row
    [:div.col-md-2 [:label "Message"]]
    [:div.col-md-5 [:input {:field :text
                            :id :msg}]]]])

(defn message-form
  []
  (let [doc (atom {})]
    (fn []
      (when @mailbox-chan
        [:div
         [bind-fields message-form-template doc]
         [:div.col-md-2 [:button.btn.btn-default
                         {:on-click (fn [] (send-message (:msg @doc)))}
                         "Send"]]]))))

(def user-id-form-template
  [:div
   [:div.row
    [:div.col-md-2 [:label "User Id"]]
    [:div.col-md-5 [:input {:field :text :id :user-id}]]]])

(defn user-id-form
  []
  (let [doc (atom {})]
    (fn []
      [:div
       [bind-fields user-id-form-template doc]
       [:div.col-md-2 [:button.btn.btn-default
                       {:on-click (fn [] (set-user (:user-id @doc)))}
                       "Set!"]]])))

(defn message-history [user-id-value message-history-value]
  (when (not-empty @user-id-value)
    [:div [:h2 @user-id-value "'s messages"]
     (for [[msg key] (map vector @message-history-value (iterate inc 1))]
       [:p {:key key} msg])]))

(defn home-page []
  [:div [:h2 "sparky-elephants"]
   [user-id-form]
   [message-form]
   [message-history user-id-value message-history-value]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
