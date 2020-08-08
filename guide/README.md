 ## Set up the LT Expertfinder
 
 #### 0. Requirements
 To successfully start developing for LT Expertfinder, you will have to install the following software:
 - Java 1.8
 - Maven
 - Python > 3
 - Docker
 - Docker-Compose
 
 #### 0.0 Clone this repository
 - Start by cloning this git repository
 - Let's assume the path on your local machine to this source code is now /path/to/lt-expertfinder/
 
 #### 0.1 Install Python Dependencies
 - Navigate to the tools directory: cd path/to/lt-expertfinder/tools/
 - Please make sure that you use Python > 3, you might need to type python3 instead of python! Check version with python3 --version or python --version
 - Create a new virtual environment: python -m venv env
 - Activate the new virtual environment: source env/bin/activate
 - Please make sure that you use the correct pip in the following command. You might need to type pip3 instead of pip!
 - Install dependencies: pip install -r requirements.txt
 - Please use this virtual environment for the following steps, if you are asked to run a python script!
 - Once finished, you can deactivate the virtual environment: deactivate

![Step 1](/guide/step1.png)

#### 1. Getting started with Docker and Docker-Compose
- Switch to the docker directory: cd /path/to/lt-expertfinder/docker
- Startup all the containers: docker-compose -f docker-compose-dev.yml up -d
- Double check that 4 containers (elasticsearch, kibana, mysql, phpmyadmin) are running: docker ps
    - The full text PDF data is stored in Elasticsearch
    - The network information (citations, publications, authors etc.) are stored in a MySQL database
- There is a common error, that elasticsearch crashes after a few seconds, due to no write permissions. In case the elasticsearch docker container is not running, try the following:
    - cd /path/to/lt-expertfinder/docker/
    - stop all docker containers: docker-compose -f docker-compose-dev.yml down
    - sudo chmod 777 data/elasticsearch/
    - restart all docker containers: docker-compose -f docker-compose-dev.yml up -d
  
![Step 2](/guide/step2.png)

#### 3.1 Download the ACL Anthology Network (AAN) data
- Download the AAN dataset from http://tangra.cs.yale.edu/newaan/index.php/home/download
- Extract the *.tar.gz, Let's assume the path on your local machine to AAN is now /path/to/aan/

![Step 3.1](/guide/step31.png)

#### 3.2 Import the ACL Anthology Network (AAN) data
- Navigate to the tools directory shipped with LT Expertfinder: cd /path/to/lt-expertfinder/tools/
- (For the next step, please make sure that the MySQL Database as well as the Elasticsearch Index are running with docker ps as we are now going to import the AAN)
- Import the AAN full text PDFs: python import_aan_elasticsearch.py -a /path/to/aan/
- Wait until the import is finished

![Step 3.2](/guide/step32.png)

#### 4. Import the MySQL Dump
In this step, you will import authors, documents, their relations (citations, collaborations, authorships), extracted keywords, WikiData crawl and GoogleScholar crawl that contain further information about each author from our provided MySQL dump.
Instead of using the MySQL Dump, you can also manually recreate the database with our scripts. However, this will take some time. If you want to construct the database yourself, please follow the instructions [here](../README.md).
- Make sure that docker containers are running: docker ps
- Copy SQL backup to MySQL Docker Container: docker cp /path/to/lt-expertfinder/datasets/mysql_dump.sql docker_mysql_1:mysql_dump.sql
- Connect to MySQL docker: docker exec -ti docker_mysql_1 bash
- Apply backup (standard pw is root): mysql -uroot -p xpertfinder < mysql_dump.sql
- exit

![Step 4](/guide/step4.png)

#### 5. Start developing!
- Open this project (/path/to/lt-expertfinder/) with the editor of your choice
![Step 5.1](/guide/step51.png)
- (For the next step, please make sure that the MySQL Database as well as the Elasticsearch Index are running with docker ps as we are now going to start the LT Expertfinder)
- Please make sure that the project SDK for this project is set to Java 1.8. (You can download the OpenJDK 1.8 [here](https://jdk.java.net/) or the Java SDK 1.8 from Oracle [here](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html), however, you will need to create an Oracle Account)
![Step 5.2](/guide/step52.png)
- Rightclick /src/main/java/de.uhh.lt.xpertfinder/Application.class and Select "Run As Java Application"
![Step 5.3](/guide/step53.png)
- Visit localhost:8080/lt-expertfinder/ui and check if everything works correctly
![Step 5.4](/guide/step54.png)
- Congratulations! You can now start developing and contribute to LT Expertfinder

#### (optional) Configuration files
If you followed this tutorial, everything is setup with the default values. You might want to change /lt-expertfinder to something else. Or if you want to change for example database name, password, Elasticsearch index etc. take a look at the following files:
- lt-expertfinder/src/main/resources/application.properties: ContextPath, Database Connection as well as Elasticsearch Connection can be configured here
- lt-expertfinder/docker/docker-compose-dev.yml: Database information and elasticsearch information can be edited here
- If you change database name, password, elasticsearch index or something like this and if you want to import the AAN data again, make sure to edit the *.py files in lt-expertfinder/tools/ before using them
