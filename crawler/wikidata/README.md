## Prerequirement
First we will need to create a file that contains all author names which we will name 'authors.csv'.
- Query the database for all author names
  - SELECT alt_name FROM authors_aan
- Export the result set as CSV
- Remove "" that surround the author names for example with a simple text editor
- Split the authors.csv file into multiple files, each containing 20 names
  - split -l20 authors.csv
- Move the new files into a directory called authors

## Crawl Wikidata
Now we will crawl wikidata and save all information in one whole.csv file.
- Run the crawl script
  - sh crawl.sh
- Wait
- Once the crawling finished, you will and up with a file called whole.csv and map.csv

## Import Wikidata
Finally, we will import all crawled data into our existing database.
- If you have not already, install mysql-connector for python 
  - pip install mysql-connector
- Make sure the mysql docker container is running (docker ps)
- Run the python script to import Wikidata
  - py import_wikidata_mysql.py -c whole.csv -m map.csv
