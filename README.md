# A BDI-based Virtual Agent for Training Child Helpline Counsellors
This repository contains three applications. 

1. dktbdiagent - this is the BDI (web) application meant for deployment using Spring Boot. 
2. dktfrontend - this contains a HTML page that makes up the frontend of the application. 
3. dktrasa - this is the Rasa application that handles the intent recognition of the user and retreives a response from the BDI application.

# Instructions
## Rasa (on local machine)
- See Rasa documentation on how to install Rasa on your machine: https://rasa.com/docs/rasa/installation
- Essentially, there are two servers you need to run - the Rasa server for the intent recognition and the custom action server that communicates with the BDI application to retreive a response. 
- To run the custom action server, use ```rasa run actions``` 
- Run the Rasa server using ```rasa run -m models --enable-api --cors  "*"```

## Setting up Rasa Chatbot on Microsoft Azure Server
The following instructions are how I was able to set up Rasa on a virtual machine hosted on Microsoft Azure. 


### Set up virtual machine
1. Create a virtual machine with the following specifications.
- image: Ubuntu 18.04 LTS - Gen 1
- vCPUs: Standard D2s v3 (2 vcpus, 8 GiB memory)
- RAM: 8 GB RAM
- authentication: SSH
- inbound ports: HTTP, HTTPS, SSH
- disk size: 64 GB (100 GB is recommended by Rasa but I made a mistake)

Everything else (networking, management, etc) was left to the default settings.

2. Set up firewall.
3. Set up DNS name. 
By default, Azure generates a dynamic IP address whenever you start your server. It's easier to set up a static one so you can use for connecting to your frontend and other applications.

4. Connect to server via SSH.


### Install Rasa X on server
I partly followed Nelle's instructions (except the part about adding Docker Hub login info. I do this through GitHub Actions. This is explained later.) since I also needed to set up a custom actions server. Here's Rasa official instructions on how to install Rasa X using Docker compose - https://storage.googleapis.com/rasa-x-releases/0.39.1/install.sh.

My Rasa specifications:
- Rasa x version: 0.39.1 (latest version as of writing) -> changed to 0.39.0
- Rasa version: 2.4.0 -> changed to 2.5.0
- Docker version 20.10.6, build 370c289
- docker-compose version 1.26.0, build d4451659
- Rasa webchat version: 1.0.1

I ran into issues later on setting up the frontend so I highly recommend that you check Rasa's compatibility matrix to install the right versions of the packages to avoid problems - https://rasa.com/docs/rasa-x/changelog/compatibility-matrix.


### Connnect Rasa X to Git repository
Additionally, I connected Rasa X to my GitHub repo. This makes it easy to update the Docker container you need for the frontend, etc. Instructions on how to do this are here - https://rasa.com/docs/rasa-x/installation-and-setup/deploy#connect-a-git-repository.


### Connect custom action server
1. First, I had to build an action server image using Docker. Instructions for this are here - https://rasa.com/docs/rasa/how-to-deploy/#building-an-action-server-image. I used the GitHub Actions to automate the image builds instead of setting it up manually using a Dockefile (as done by Nelle). This way, Docker rebuilds the image whenever changes are made to the actions folder and pushed to the main branch. Optionally, you can include automatic upgrades for Rasa X but I didn't do this to avoid breaking things.

2. Optionally, you can add the image to Azure Container Registry - https://azure.microsoft.com/en-us/services/container-registry/
3. Connect the image to Rasa X in your docker-compose.override.yml file - https://rasa.com/docs/rasa-x/installation-and-setup/customize#connecting-a-custom-action-server

```in docker-compose.override.yml:
version: '3.4'
services:
  app:
    image: <username/image:tag>
 ```
 4. If your Docker containers are already running, take them down and then start Rasa X again:

```
cd /etc/rasa
sudo docker-compose down
sudo docker-compose up -d
 ```


## Rasa Webchat (Frontend)
1. I made an HTML file with the script for Rasa webchat as follows. Instructions for Rasa Webchat can be found here -  https://github.com/botfront/rasa-webchat. I used version ```rasa-webchat@1.0.1```  

```
      <script>!(function () {
        let e = document.createElement("script");
          t = document.head || document.getElementsByTagName("head")[0];
        (e.src =
          "https://cdn.jsdelivr.net/npm/rasa-webchat@1.0.1/lib/index.js"),
          // Replace 1.x.x with the version that you want
          (e.async = !0),
          (e.onload = () => {
            window.WebChat.default(
              {
                // initPayload: "request_greeting_unknown",
                customData: { language: "nl" },
                socketUrl: "http://<serverIP>",
                socketPath: "/socket.io/",
                title: "Chat met Lilobot",
                showFullScreenButton: true,
                embedded: false,
                inputTextFieldHint:"Typ een bericht",
                profileAvatar: "images/bot_icon.png",
                docViewer: false,
                params: {
                  storage: "session",
                }
              },
              null
            );
          }),
          t.insertBefore(e, t.firstChild);
        })();
      </script>
```

2. I created a Dockerfile in the same directory as my html file. 

```
FROM nginx:alpine
COPY . /usr/share/nginx/html

```

3. Build using docker as follows:
```
docker build . -t <repository>:<version>
```

4. Tag the image and push to Docker hub.
```
docker tag <imageID> <username>/<repository>:<version>
docker push <username>/<repository>:<version>
```

5. On my remote server, I pulled the image from Docker Hub (I'm already logged into Docker) and ran the container using the commands,
```
docker pull <username>/<repository>:<version>
sudo docker run -d -p 5009:80 <username>/<repository>:<version>
```
Remember to open the inbound port of the VM in Azure to be able to see the webpage. You can use this command if you have Azure CLI installed on your local machine: 
```
az vm open-port --resource-group <resourceGroupName> --name <vmName> --port 5009
```

Note: You might have to start this container on starting up your server.



## Setting up BDI (Spring Boot) application on Microsoft Azure
### Local
 1. Build the JAR file of application and run locally for testing. Make sure you have Postgres set up already so the application doesn't crash. Instructions for this are down below.

### Configure and deploy the app to Azure Spring Cloud
There are two options for running a Spring boot application easily on Azure. One is using an Azure Spring Cloud cluster and the other is using an Azure App Service. The former is more expensive and since the service was not hosting that many users, I chose the second option. 

1. Provision an Azure App Service through the Azure portal. I used Github Actions to build and manage the deployment of the web app. More information here: https://docs.github.com/en/actions/guides/deploying-to-azure-app-service

I used Github Secrets to manage the login credentials to the Azure service to make updates easy. 

### Configure Blob Storage for reports (for remote server only, otherwise use local file system)
1. The BDI agent creates a Word document (.docx) with the BDI status and transcript of the conversation. I store these files on Azure Blob Storage. Follow the instructions below to create and connect the storage to the app service.

https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java?tabs=powershell#configure-your-storage-connection-string

2. I ran into issues trying to get the Rasa frontend to download the file. This works however is the server is hosted locally. Here are the instructions for that.
https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java?tabs=powershell#download-blobs

Instead, I generate SAS tokens for the user to access the file directly from Azure. There is no need for the Rasa frontend to download it anymore. Here are the instructions for that: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-blob#generate-a-sas-token. 

3. Include the keys to the ReportService of the BDI application.


## Setting up Postgres database
### on local machine
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

### on Microsoft Azure
1. Configure a server instance (Azure Database for PostgreSQL server) on Azure.
2. Configure firewall. I set mine to accept traffic from my personal computer (for debugging purposes) and the Spring application (which should be the only one talking to the database). Make sure to include *all* the outbound IP addresses from the Azure web app.
3. Connect to the server using a postgres client to make sure everything works. I use psql. 

    ```psql "host=<host url> port=5432 dbname=postgres user=<user> password=<password> sslmode=require"```

4. Create database for BDI spring application and configure access.

```
CREATE DATABASE dktbase;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE "dktbase" TO postgres;
 ```
 
5. Optionally, you can create a database for Rasa event broker to store conversations (with id, sender_id, data) directly from Rasa. I ended up forgoing this because I created a report service in my BDI application that stores the conversation instead. 

```
CREATE DATABASE rasabase;
CREATE USER rasa WITH PASSWORD 'rasa';
GRANT ALL PRIVILEGES ON DATABASE "rasabase" TO rasa;
 ```

More information here: https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/postgresql/quickstart-create-server-database-portal.md

6. Include the credentials of your database in the BDI application (in application.properties) in your Github repository. 
```
spring.datasource.url=jdbc:postgresql://<server-name>:5432/dktbase
spring.datasource.username=postgres
spring.datasource.password=postgres
```
You should be able to run your BDI application successfully now. A more secure option is to use Github password manager (or whatever software management tool you're using) to save the credentials instead so your password isn't exposed.

# Resources
Links to data related to this thesis. 
- Experiment data (4TU.ResearchData): https://doi.org/10.4121/17371919
- OSF form: Evaluation of a BDI-based virtual agent for training child helpline counsellors - https://osf.io/hkxzc
- Project storage TU Delft: U:\MScProjectDKT (owned by Merijn Bruijnes)
- Thesis report: http://resolver.tudelft.nl/uuid:f04f8f0b-9ab9-4f1c-a19c-43b164d45cce



Here are some handy links I used throughout the thesis. 
- Data analysis markdown file (Willem-Paul):  https://data.4tu.nl/repository/uuid:0cf03876-0f94-4225-b211-c5971d250002
- Data management plan: https://dmponline.tudelft.nl
- Data science research lectures (Willem-Paul): http://yukon.twi.tudelft.nl/weblectures/cs4125.html 
- De Kindertelefoon e-learning: https://www.linkidstudy.nl
- Human research ethics committee (HREC): https://www.tudelft.nl/over-tu-delft/strategie/integriteitsbeleid/human-research-ethics
  - HREC application: https://labservant.tudelft.nl/
  - Template Informed Consent Form: https://www.tudelft.nl/over-tu-delft/strategie/integriteitsbeleid/human-research-ethics/template-informed-consent-form
- Qualtrics TU Delft: https://tudelft.eu.qualtrics.com/
- OSF form: https://osf.io
  - Computer-based intervention for supporting individuals in changing negative thinking patterns: https://osf.io/v6tkq
  - A support system for people with social diabetes distress: https://osf.io/yb6vg
  - Study on effects of a virtual reality exposure with eye-gaze adaptive virtual cognitions: https://osf.io/q58v4
- Rasa: https://rasa.com
- Remote desktop (weblogin) TU Delft: https://weblogin.tudelft.nl/
- Self service portal TU Delft: https://tudelft.topdesk.net
- Transtheoretical model: https://en.wikipedia.org/wiki/Transtheoretical_model
- Virtual human toolkit: https://vhtoolkit.ict.usc.edu
- System Usability Scale: https://www.usability.gov/how-to-and-tools/methods/system-usability-scale.html
  - SUS in Dutch: https://www.usersense.nl/usability-testing/system-usability-scale-sus


# Contact
Feel free to contact me (afua.grundmann@gmail.com) if you have any questions about this project.
