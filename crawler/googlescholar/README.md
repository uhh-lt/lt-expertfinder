## Prerequirement
First we will need to create a file that contains all author names which we will name 'authors.csv'.
- Query the database for all author names
  - SELECT alt_name FROM authors_aan
- Export the result set as CSV
- Remove "" that surround the author names for example with a simple text editor

## Crawl Google Scholar
Next, we will crawl google scholar and save all information as .json files.
- java -jar googlescholarcrawler.jar authors.csv
- wait
- Once the crawling is finished, you will end up with a directory called authors

## Import Google Scholar
Finally, we will import all crawled data into our existing database.
- If you have not already, install mysql-connector for python 
  - pip install mysql-connector
- Make sure the mysql docker container is running (docker ps)
- Run the python script to import Google Scholar
  - py import_googlescholar_mysql.py -p authors
