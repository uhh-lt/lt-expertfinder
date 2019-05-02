import json,sys,io,os
from elasticsearch import Elasticsearch
from argparse import ArgumentParser

def main():
	parser = ArgumentParser()
	parser.add_argument("-a", "--aan", dest="aan", help="path to AAN", metavar="AAN")
	
	args = parser.parse_args()
	
	aan_path = os.path.normpath(args.aan)
	papers = "papers_text"
	papers_path = os.path.normpath(aan_path+"/"+papers)

	es = Elasticsearch(['localhost'], port=9200)
	
	for entry in os.scandir(papers_path): 
		filepath = entry.path
		filename = entry.name
		if filename.endswith(".txt"): 
			print("Indexing "+filename)
			# extract id from filename
			id = os.path.splitext(filename)[0]
			with io.open(filepath, 'r', encoding="utf8") as file:
				# read file
				file_data = file.read()
				# create json object
				data_obj = {'text': file_data}
				json_data = json.dumps(data_obj)
				# import to elasticsearch
				es.index(index='aan', doc_type="_doc", id=id, body=json_data)
		else:
			continue

if __name__ == "__main__":
	main()

