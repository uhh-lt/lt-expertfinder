# LT Expertfinder
This is the repository of LT Expertfinder, an open source evaluation framework for expert finding methods. The LT Expertfinder enables you to compare, evaluate and use different expert finding methods. To leverage the evaluation process, LT Expertfinder provides many different tools that will help to a obtain detailed insights into the expert finding methods. 

Currently, the tool operates on the ACL Anthology Network (http://tangra.cs.yale.edu/newaan/). However, by setting up your own server, you can exchange the data as you wish. Also, we are currently working on expanding the dataset with the papers from arXiv (https://arxiv.org/).

A running version of this tool can be found at http://ltdemos.informatik.uni-hamburg.de/lt-expertfinder/ui.

A short demonstration video is also availale at https://youtu.be/A4yRZezWUvE.

The demonstration paper "LT Expertfinder: An Evaluation Framework for Expert Finding Methods" will be published in NAACL 2019 and can be read at https://www.inf.uni-hamburg.de/en/inst/ab/lt/publications/2019-fischeretal-naacldemo-expertfinder.pdf.
 
 ## Expert Finding Methods
 The LT Expertfinder ships already with some basic expert finding methods:
 - Model2 by Balog et. al.
 - K-step Random Walk by Serdyukov et. al.
 - Infinite Random Walk by Serdyukov et. al.
 - Weighted Relevance Propagation
 - PageRank
 - H-Index Ranking (simple baseline method)
 - Citation Ranking (simple baseline method)
 
 If you wish to add your own expert finding method and use the tool to compare it see the section below.
 
 ## Set up the LT Expertfinder
 
 #### 0. Requirements
 To successfully start developing for LT Expertfinder, you will have to install the following software:
 - Java 1.8
 - Maven
 - Python > 3
     - Elasticsearch module: pip install elasticsearch
     - MySQL module: pip install mysql-connector
 - Docker
 - Docker-Compose
 
 #### 0.1 Install Python Dependencies
 - Navigate to the tools directory: cd .../lt-expertfinder/tools/
 - Create a new virtual environment: python3 -m venv env
 - Activate the new virtual environment: source env/bin/activate
 - Install dependencies: pip3 install -r requirements.txt
 - Please use this virtual environment for the following steps, if you are asked to run a python script!
 - Once finished, you can deactivate the virtual environment: deactivate

 
### Set up your own LT Expertfinder for Development
 While the LT Expertfinder is already a good tool to use, compare and evaluate our pre-implemented expert finding methods, you might want to add you own methods by setting up your own instance of LT Expertfinder and expanding the source code. Implementing your own expert finding method will enable you to compare it to the already existing methods. If you are intereseted in this, follow the steps below, otherwise, if you just want to use the existing methods we recommend you to use the running version of this tool.

#### 1. Get the source code
- Start by cloning this git repository
- Let's assume the path on your local machine to this source code is now /path/to/lt-expertfinder/

#### 2. Getting started with Docker and Docker-Compose
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

#### 3.1 Import the ACL Anthology Network (AAN) data
- Download the AAN dataset from http://tangra.cs.yale.edu/newaan/index.php/home/download
- Extract the *.tar.gz, Let's assume the path on your local machine to AAN is now /path/to/aan/
- Navigate to the tools directory shipped with LT Expertfinder: cd /path/to/lt-expertfinder/tools/
- (For the next step, please make sure that the MySQL Database as well as the Elasticsearch Index are running with docker ps as we are now going to import the AAN)
- Import the AAN full text PDFs: python import_aan_elasticsearch.py -a /path/to/aanrelease2014/aan/
    - This will take a while, you can continue with the next import
- Import the network information: python import_aan_mysql.py -a /path/to/aan/
- Wait until the Imports are finished

#### 3.2 Import the GoogleScholar Crawl
- Navigate to the datasets directory: cd /path/to/lt-expertfinder/datasets/
- Find the file called google_scholar_crawl.tar.gz and extract the contents to a folder of your choice. Now, let's assume
  the path to the extracted google_scholar_crawl directory is /path/to/google_scholar_crawl/
- Navigate to the tools directory: cd /path/to/lt-expertfinder/tools/
- Make sure that the docker containers are running: docker ps
- Run the GoogleScholar Crawl import script: python import_googlescholarmysql.py -p /path/to/google_scholar_crawl

#### 3.3 Import the WikiData Crawl
- Navigate to the datasets directory: cd /path/to/lt-expertfinder/datasets/
- Find the file called wikidata_crawl.tar.gz and extract the contents to a folder of your choice. Now, let's assume
  the path to the extracted wikidata_crawl directory is /path/to/wikidata_crawl/
- Navigate to the tools directory: cd /path/to/lt-expertfinder/tools/
- Make sure that the docker containers are running: docker ps
- Run the WikiData Crawl import script: python import_wikidata_mysql.py -p /path/to/wikidata_crawl

#### 3.4 Extract Keywords
- Navigate to the tools directory: cd /path/to/lt-expertfinder/tools/
- Make sure that the docker containers are running: docker ps
- Make sure that you have Java Version 1.8: java -version
- Run the Keyword Extraction tool: java -jar import_keywords.jar -dbn xpertfinder -ei aan
- This will take a while!

#### 4. Start developing!
- Open this project (/path/to/lt-expertfinder/) with the editor of your choice
- (For the next step, please make sure that the MySQL Database as well as the Elasticsearch Index are running with docker ps as we are now going to start the LT Expertfinder)
- Rightclick /src/main/java/de.uhh.lt.xpertfinder/Application.class and Select "Run As Java Application"
    - If you have a newer version of Java installed, please make sure that the project SDK for this project is set to Java 1.8
- Visit localhost:8080/lt-expertfinder/ui and check if everything works correctly
- Congratulations! You can now start developing and contribute to LT Expertfinder

#### (optional) Configuration files
If you followed this tutorial, everything is setup with the default values. You might want to change /lt-expertfinder to something else. Or if you want to change for example database name, password, Elasticsearch index etc. take a look at the following files:
- lt-expertfinder/src/main/resources/application.properties: ContextPath, Database Connection as well as Elasticsearch Connection can be configured here
- lt-expertfinder/docker/docker-compose-dev.yml: Database information and elasticsearch information can be edited here
- If you change database name, password, elasticsearch index or something like this and if you want to import the AAN data again, make sure to edit the *.py files in lt-expertfinder/tools/ befor using them

## Set up your own LT Expertfinder Server
If you are not interested in developing LT Expertfinder and just want to host your own LT Expertfinder Server, please follow the instructions below:

#### 1. Get the source code
- Start by cloning this git repository
- Let's assume the path on your local machine to this source code is now /path/to/lt-expertfinder/
    - You will only need /docker and /tools, you can delete /src if you want

#### 2. Start the Docker Containers
- Switch to the docker directory: cd /path/to/lt-expertfinder/docker
- Startup all the containers: docker-compose -f docker-compose-prod.yml up -d
-- Please note that this file utilizes our latest published Docker container of LT Expertfinder that is available at https://cloud.docker.com/u/uhhlt/repository/docker/uhhlt/xpertfinder

#### 3.1 Import the ACL Anthology Network (AAN) data
- Download the AAN dataset from http://tangra.cs.yale.edu/newaan/index.php/home/download
- Extract the *.tar.gz, Let's assume the path on your local machine to AAN is now /path/to/aanrelease2014/
- Navigate to the tools directory shipped with LT Expertfinder: cd /path/to/lt-expertfinder/tools/
- (For the next step, please make sure that the Elasticsearch Docker is running with docker ps as we are now going to import the AAN)
- Import the AAN full text PDFs: python import_aan_elasticsearch.py -a /path/to/aan/
    - This will take a while, you can continue with the next import
- Wait until the import is finished

#### 3.2 Import the MySQL Dump
In this step, you will import authors, documents, their relations (citations, collaborations, authorships), extracted keywords, WikiData crawl and GoogleScholar crawl that contain further information about each author from our provided MySQL dump.
Instead of using the MySQL Dump, you can also manually recreate the database with our scripts. However, this will take some time. If you want to contruct the database yourself, please read the instructions here.
- Make sure that docker containers are running: docker ps
- Copy SQL backup to MySQL Docker Container: docker cp /path/to/lt-expertfinder/datasets/mysql_dump.sql docker_mysql_1:mysql_dump.sql
- Connect to MySQL docker: docker exec -ti docker_mysql_1 bash
- Apply backup: mysql -uroot -p xpertfinder < xpertfinder_backup.sql
- exit

#### 4. Restart the Docker Containers
- Switch to the docker directory: cd /path/to/lt-expertfinder/docker
- Stop all the containers: docker-compose -f docker-compose-prod.yml down
- Start all the containers: docker-compose -f docker-compose-prod.yml up -d
- Visit localhost:8080/lt-expertfinder/ui and start expert finding :)
