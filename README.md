# Emotion-BDI: Toward a Believable Child Helpline Training Tool
This repository contains three applications, which were extended based on the original Lilobot developed as part of a master thesis by Sharon Grundmann.

1. dktbdiagent - this is the Emotion-BDI (web) application meant for deployment using Spring Boot. 
2. dktfrontend - this contains a HTML page that makes up the frontend of the application. 
3. dktrasa - this is the Rasa application that handles the intent recognition of the user and retreives a response from the BDI application.

# Instructions
## Rasa
- See Rasa documentation on how to install Rasa on your machine: https://rasa.com/docs/rasa/installation
- Essentially, there are two servers you need to run - the Rasa server for the intent recognition and the custom action server that communicates with the BDI application to retreive a response. 
- To run the custom action server, use ```rasa run actions``` 
- Run the Rasa server using ```rasa run -m models --enable-api --cors  "*"```

## Setting up BDI (Spring Boot) application
Build the JAR file of application and run locally for testing. Make sure you have Postgres set up already so the application doesn't crash. Instructions for this are down below.

## Setting up Postgres database
1. Install a postgres server on your local machine. You can find instructions for this through a Google search.  
2. Create database for BDI application.
```
CREATE DATABASE dktbase;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE "dktbase" TO postgres;
 ```

3. Include the credentials of your database in the BDI application (in application.properties). The BDI application requires this, otherwise the application will crash.
```
spring.datasource.url=jdbc:postgresql://localhost:5432/dktbase
spring.datasource.username=postgres
spring.datasource.password=postgres
```
You should be able to run your BDI application successfully now. 

# Resources

Links to data related to this thesis. 
- Experiment data (4TU.ResearchData): [https://doi.org/10.4121/17371919](https://doi.org/10.4121/ad16f513-3e07-4840-aed9-45f0de6b00c0)
- OSF form: Evaluation of a BDI-based virtual agent for training child helpline counsellors - [https://osf.io/hkxzc](https://osf.io/k5p8r)
- Thesis report: [http://resolver.tudelft.nl/uuid:f04f8f0b-9ab9-4f1c-a19c-43b164d45cce](https://resolver.tudelft.nl/uuid:d07f81de-35de-4a2c-a363-9169d53c90b5)

# Contact
Feel free to contact me (d.lu@uu.nl) if you have any questions about this project.
