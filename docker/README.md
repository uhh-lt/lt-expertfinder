## How To Dockerize LT Expertfinder
### Build LT Expertfinder Application
- mvn assembly:assembly
- cd ~/Development/git/lt-expertfinder
- mv target/xpertfinder-1.0.jar docker/docker-ltexpertfinder/
- cd docker/docker-ltexpertfinder
- docker build -t uhhlt/xpertfinder --build-arg JAR_FILE=xpertfinder-1.0.jar .

### Push the LT Expertfinder Docker container
- docker login --username=bigabig
- docker push uhhlt/xpertfinder:latest

### Use the new Docker on our server
- sshrz 5fischer@ltdocker
- cd srv/docker/sr-xpertfinder/
- docker-compose down (if running)
- docker pull uhhlt/xpertfinder:latest
- docker-compose up -d (start again)

## How to quickly setup LT Expertfinder on server

### Upload MySQL settings to our server
- Login on server: sshrz 5fischer@ltdocker
- mkdir /srv/docker/sr-xpertfinder/mysql-config
- ON LOCAL MACHINE: rsyncrz -avvzhP /path/to/lt-expertfinder/docker/config/mysql/config-file.cnf 5fischer@ltdocker:/srv/docker/sr-xpertfinder/mysql-config/

### Create Backup of local MySQL database
- Make sure that docker containers are running: docker ps
- Connect to MySQL docker: docker exec -ti docker_mysql_1 bash
- Create backup: mysqldump -uroot -p --routines xpertfinder authors_aan citations_aan collaborations_aan collaborations_aan2 documents_aan google_authors keywords_best keywords_all publications_aan wikidata > xpertfinder_backup.sql
- exit
- docker cp docker_mysql_1:/xpertfinder_backup.sql .

### Apply SQL Backup on our server
- Upload SQL backup to the server: rsyncrz -avvzhP xpertfinder_backup.sql 5fischer@ltdocker:/srv/docker/sr-xpertfinder/
- Login on server: sshrz 5fischer@ltdocker
- Make sure that docker containers are running: docker ps
- Copy SQL backup to MySQL Docker Container: docker cp /srv/docker/sr-xpertfinder/xpertfinder_backup.sql srxpertfinder_mysql_1:xpertfinder_backup.sql
- Connect to MySQL docker: docker exec -ti srxpertfinder_mysql_1 bash
- Apply backup: mysql -uroot -p xpertfinder < xpertfinder_backup.sql

### Import AAN Documents into our server's Elasticsearch
Upload AAN to the server
- Find Local AAN dataset: cd /path/to/aan/
- Compress AAN (if needed): tar cfvz aan.tar.gz aan
- Upload AAN Data to our server: rsyncrz -avvzhP aan.tar.gz 5fischer@ltdocker:/srv/docker/sr-xpertfinder/
- Upload AAN import script to our server: rsyncrz -avvzhP /path/to/lt-expertfinder/tools/import_aan_elasticsearch.py 5fischer@ltdocker:/srv/docker/sr-xpertfinder/
- Upload requirements.txt to our server: rsyncrz -avvzhP /path/to/lt-expertfinder/tools/requirements.txt 5fischer@ltdocker:/srv/docker/sr-xpertfinder/
Extract the AAN on the server
- Login on server: sshrz 5fischer@ltdocker
- cd /srv/docker/sr-xpertfinder/
- Decompress AAN: tar -xzf aan.tar.gz
Import AAN into elasticsearch
- Create a new virtual environment: python3 -m venv env
- Activate the new virtual environment: source env/bin/activate
- Install dependencies: pip3 install -r requirements.txt
- Run AAN import script: python import_aan_mysql.py -a /path/to/aanrelease2014/aan/

### Restart docker container on server 
- sshrz 5fischer@ltdocker
- cd srv/docker/sr-xpertfinder/
- docker-compose down (if running)
- docker-compose up -d (start again)