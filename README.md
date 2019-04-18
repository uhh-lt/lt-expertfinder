# LT Expertfinder
This is the repository of LT Expertfinder, an open source evaluation framework for expert finding methods. The LT Expertfinder enables you to compare, evaluate and use different expert finding methods. To leverage the evaluation process, LT Expertfinder provides many different tools that will help to a obtain detailed insights into the expert finding methods. 

Currently, the tool operates on the ACL Anthology Network (http://tangra.cs.yale.edu/newaan/). However, by setting up your own server, you can exchange the data as you wish. Also, we are currently working on expanding the dataset with the papers from arXiv (https://arxiv.org/).

A running version of this tool can be found at http://ltdemos.informatik.uni-hamburg.de/lt-expertfinder/ui.

A short demonstration video is also availale at https://youtu.be/A4yRZezWUvE.

The demonstration paper "LT Expertfinder: An Evaluation Frameworkfor Expert Finding Methods" will be published in NAACL 2019 and can be viewd at <Link>.
 
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
 
 ## Set up your own LT Expertfinder for Development
 While the LT Expertfinder is already a good tool to use, compare and evaluate our pre-implemented expert finding methods, you might want to add to these methods by setting up your own instance of LT Expertfinder and expanding the source code. Implementing your own expert finding method will enable you to compare it to the already existing methods. If you are intereseted in this, follow the steps below, otherwise, if you just want to use or the existing methods we recommend you to use the running version of this tool.

#### 0. Requirements
To successfully start developing for LT Expertfinder, you will have to install the following software:
- Java 1.8
- Maven
- Python > 3
    - Elasticsearch module: pip install elasticsearch
    - MySQL module: pip install mysql-connector
- Docker
- Docker-Compose

#### 1. Get the source code
- Start by cloning this git repository
- Let's assume the path on your local machine to this source code is now /path/to/lt-expertfinder/

#### 2. Getting started with Docker and Docker-Compose
- Switch to the docker directory: cd /path/to/lt-expertfinder/docker
- Startup all the containers: docker-compose -f docker-compose-dev.yml up -d
- Double check that 4 containers (elasticsearch, kibana, mysql, phpmyadmin) are running: docker ps
    - The full text PDF data is stored in Elasticsearch
    - The network information (citations, publications, authors etc.) are stored in a MySQL database

#### 3. Import the ACL Anthology Network (AAN) data
- Download the AAN dataset from http://tangra.cs.yale.edu/newaan/index.php/home/download
- Extract the *.tar.gz, Let's assume the path on your local machine to AAN is now /path/to/aanrelease2014/
- Navigate to the tools directory shipped with LT Expertfinder: cd /path/to/lt-expertfinder/tools/
- (For the next step, please make sure that the MySQL Database as well as the Elasticsearch Index are running with docker ps as we are now going to import the AAN)
- Import the AAN full text PDFs: python import_aan_elasticsearch.py -a /path/to/aanrelease2014/aan/
    - This will take a while, you can continue with the next import
- Import the network information: python import_aan_mysql.py -a /path/to/aanrelease2014/aan/
- Wait until the Imports are finished

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

#### 0. Requirements
To quickly setup your own LT Expertfinder Server, you will have to install the following software:
- Docker
- Docker-Compose

#### 1. Get the source code
- Start by cloning this git repository
- Let's assume the path on your local machine to this source code is now /path/to/lt-expertfinder/
    - You will only need /docker and /tools, you can delete /src if you want

#### 2. Start the Docker Containers
- Switch to the docker directory: cd /path/to/lt-expertfinder/docker
- Startup all the containers: docker-compose -f docker-compose-prod.yml up -d
-- Please note that this file utilizes our latest published Docker container of LT Expertfinder that is available at https://cloud.docker.com/u/uhhlt/repository/docker/uhhlt/xpertfinder

#### 3. Import the ACL Anthology Network (AAN) data
- Download the AAN dataset from http://tangra.cs.yale.edu/newaan/index.php/home/download
- Extract the *.tar.gz, Let's assume the path on your local machine to AAN is now /path/to/aanrelease2014/
- Navigate to the tools directory shipped with LT Expertfinder: cd /path/to/lt-expertfinder/tools/
- (For the next step, please make sure that the MySQL Database as well as the Elasticsearch Index are running with docker ps as we are now going to import the AAN)
- Import the AAN full text PDFs: python import_aan_elasticsearch.py -a /path/to/aanrelease2014/aan/
    - This will take a while, you can continue with the next import
- Import the network information: python import_aan_mysql.py -a /path/to/aanrelease2014/aan/
- Wait until the Imports are finished

#### 4. Restart the Docker Containers
- Switch to the docker directory: cd /path/to/lt-expertfinder/docker
- Stop all the containers: docker-compose -f docker-compose-prod.yml down
- Start all the containers: docker-compose -f docker-compose-prod.yml up -d
- Visit localhost:8080/lt-expertfinder/ui and start expert finding :)