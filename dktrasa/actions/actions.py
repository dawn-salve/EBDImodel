# This files contains your custom actions which can be used to run
# custom Python code.
#
# See this guide on how to implement these action:
# https://rasa.com/docs/rasa/custom-actions

BDIAGENT_ENDPOINT = "http://localhost:8080/agent/"
REPORT_ENDPOINT = "http://localhost:8080/report/"
LOCALMODE = True  # change to false if using Azure Blob Storage

# Azure configuration
# BDIAGENT_ENDPOINT = "http://dktspringapp.azurewebsites.net/agent/"
# REPORT_ENDPOINT = "http://dktspringapp.azurewebsites.net/report/"

from typing import Any, Text, Dict, List
import datetime

import json
import subprocess
import random
import requests

from rasa_sdk import Action, Tracker
from rasa_sdk.events import ReminderScheduled, ReminderCancelled
from rasa_sdk.executor import CollectingDispatcher


class ActionUseBdi(Action):

    def name(self) -> Text:
        return "action_use_bdi"

    async def run(self, dispatcher: CollectingDispatcher,
                  tracker: Tracker,
                  domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        conversation_id = tracker.sender_id

        text = tracker.latest_message["text"]
        intent_name = tracker.latest_message["intent"].get("name")

        if intent_name == 'nlu_fallback':
            return

        headers = {'Content-Type': "application/json", }

        payloadArray = intent_name.split("_")
        print(payloadArray)

        # todo: handle nlu fallback
        payload = {"type": payloadArray[0], "subject": payloadArray[1], "attribute": payloadArray[2], "text": text}

        response = requests.post(url=BDIAGENT_ENDPOINT + conversation_id, data=json.dumps(payload), headers=headers)

        if "timestamp" not in response.text:
            dispatcher.utter_message(text=response.text)

        return []


class ActionSetReminder(Action):
    """Schedules a reminder between 7 and 20 seconds to trigger the BDI agent to say something if user is inactive."""

    def name(self) -> Text:
        return "action_set_reminder"

    async def run(
            self,
            dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any],
    ) -> List[Dict[Text, Any]]:
        time = 300
        date = datetime.datetime.now() + datetime.timedelta(seconds=time)

        reminder = ReminderScheduled(
            "EXTERNAL_reminder",
            trigger_date_time=date,
            name="my_reminder",
            kill_on_user_message=True,
        )

        return [reminder]


class ActionReactToReminder(Action):
    """Triggers (remindst) the BDI agent to say something based on its intention."""

    def name(self) -> Text:
        return "action_react_to_reminder"

    async def run(
            self,
            dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any],
    ) -> List[Dict[Text, Any]]:
        print("calling bdi with trigger")

        conversation_id = tracker.sender_id

        intent_name = tracker.latest_message["intent"].get("name")

        headers = {'Content-Type': "application/json", }

        payload = {"type": "trigger", "subject": "", "attribute": ""}

        response = requests.post(url=BDIAGENT_ENDPOINT + conversation_id, data=json.dumps(payload), headers=headers)

        dispatcher.utter_message(text=response.text)

        return []


class ForgetReminders(Action):
    """Cancels all reminders (triggers for BDI to say something)."""

    def name(self) -> Text:
        return "action_forget_reminders"

    async def run(
            self, dispatcher, tracker: Tracker, domain: Dict[Text, Any]
    ) -> List[Dict[Text, Any]]:
        print("cancelling reminders")

        return [ReminderCancelled()]


class ActionPrintBdi(Action):
    """Prints conversation and BDI status"""

    def name(self) -> Text:
        return "action_get_bdi"

    async def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: Dict[Text, Any]) -> List[
        Dict[Text, Any]]:

        conversation_id = tracker.sender_id
        response = requests.get(url=REPORT_ENDPOINT + conversation_id)
        doc_path = response.text
        print(doc_path)

        if LOCALMODE:
            dispatcher.utter_message(text="The conversation is over. Here's your transcript: " + doc_path)

        else:
            dispatcher.utter_message(
                "[![download transcript](https://img.icons8.com/windows/452/download--v1.png)](" + doc_path + ")")
            dispatcher.utter_message(text="The conversation is over. You can now download a transcript of it by "
                                          "clicking the arrow above.")

        return []


