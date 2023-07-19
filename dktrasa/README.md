# Lilobot - DKT Rasa Chatbot

Part of MSc Thesis by Sharon Afua Grundmann. This Rasa application handles intent recognition of a user's input. Once an intent is recognized, the application sends the intent name to the BDI service (hosted separately) to reason and relays the response to the user. actions.py expects the BDI service to be running locally. If using a remote server, change the endpoint to the appropriate URL. 

## Requirements
- Rasa x version: 0.39.0
- Rasa version: 2.5.0
- Docker version 20.10.6, build 370c289
- Docker-compose version 1.26.0, build d4451659
- Rasa webchat version: 1.0.1
- spaCy version: 3.0.6
- spaCy model: nl_core_news_lg (3.0.0)


Instructions on how to get everything running is in the README of the root folder.