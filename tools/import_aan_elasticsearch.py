import json,sys,io,os
from pprint import pprint
from argparse import ArgumentParser
from elasticsearch import Elasticsearch

def main():
	es = Elasticsearch(['localhost'], port=9200)
	path = "papers_text"
	
	for entry in os.scandir(path): 
		filepath = entry.path
		filename = entry.name
		if filename.endswith(".txt"): 
			print("Indexing "+filename)
			# extract id from filename
			id = os.path.splitext(filename)[0]
			with io.open(filepath, 'r', encoding="utf8") as file:
				# read file
				file_data = file.read();
				# create json object
				data_obj = {}
				data_obj['text'] = file_data
				json_data = json.dumps(data_obj)
				# import to elasticsearch
				es.index(index='aan', doc_type="_doc", id=id, body=json_data)
		else:
			continue

if __name__ == "__main__":
	main()

