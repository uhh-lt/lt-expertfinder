import json,sys,io,os
from elasticsearch import Elasticsearch
from argparse import ArgumentParser

def main():
	parser = ArgumentParser()
	parser.add_argument("-a", "--aan", dest="aan", help="path to AAN", metavar="AAN")
	parser.add_argument("-t", "--timeout", dest="timeout", help="maximum wait time in seconds for success of one bulk import", metavar="TIMEOUT")

	args = parser.parse_args()

	if not args.aan:
		parser.print_help()
		return

	timeout = 30
	if args.timeout:
		timeout = int(float(args.timeout))

	aan_path = os.path.normpath(args.aan)
	papers = "papers_text"
	papers_path = os.path.normpath(aan_path+"/"+papers)

	es = Elasticsearch(['localhost'], port=9200)
	index = "aan"
	mapping = {   "mappings": {
		"_doc": {
			"properties": {
				"text": {
					"type": "text",
					"fields": {
						"length": {
							"type": "token_count",
							"analyzer": "standard",
							"store": "true"
						}
					},
					"term_vector": "yes",
					"store" : True,
					"analyzer" : "fulltext_analyzer"
				},
				"doc": {
					"properties": {
						"text": {
							"type": "text",
							"fields": {
								"keyword": {
									"type": "keyword",
									"ignore_above": 256
								}
							}
						}
					}
				},
				"field_statistics": {
					"type": "boolean"
				},
				"fields": {
					"type": "text",
					"fields": {
						"keyword": {
							"type": "keyword",
							"ignore_above": 256
						}
					}
				},
				"offsets": {
					"type": "boolean"
				},
				"positions": {
					"type": "boolean"
				},
				"term_statistics": {
					"type": "boolean"
				}
			}
		}
	},
		"settings" : {
			"index" : {
				"number_of_shards" : 1,
				"number_of_replicas" : 0
			},
			"analysis": {
				"analyzer": {
					"fulltext_analyzer": {
						"type": "custom",
						"tokenizer": "whitespace",
						"filter": [
							"lowercase",
							"type_as_payload"
						]
					}
				}
			}
		}
	}

	if es.indices.exists(index):
		print("deleting '%s' index..." % index)
		res = es.indices.delete(index = index)
		print(" response: '%s'" % res)
	print("creating '%s' index..." % index)
	res = es.indices.create(index = index, body = mapping)
	print(" response: '%s'" % res)

	# read all data into array
	bulk_data = []
	counter = 0
	for entry in os.scandir(papers_path):
		filepath = entry.path
		filename = entry.name
		if filename.endswith(".txt"):
			# print("Indexing "+filename)
			# extract id from filename
			id = os.path.splitext(filename)[0]
			with io.open(filepath, 'r', encoding="utf8") as file:
				# read file
				file_data = file.read()
				# create json object
				data_obj = {'text': file_data}
				json_data = json.dumps(data_obj)
				# create header object
				op_dict = {
					"index": {
						"_index": index,
						"_type": "_doc",
						"_id": id
					}
				}
				# import to elasticsearch
				# es.index(index='aan', doc_type="_doc", id=id, body=json_data)
				bulk_data.append(op_dict)
				bulk_data.append(json_data)

				# bulk import into elasticsearch
				counter = counter + 1
				if counter % 500 == 0:
					print("Importing bulk...")
					es.bulk(index = index, body = bulk_data, refresh = True, request_timeout=timeout)
					bulk_data = []
		else:
			continue

	# bulk import into elasticsearch
	print("Importing bulk...")
	es.bulk(index = index, body = bulk_data, refresh = True)

	print("Finished importing AAN! :)")

if __name__ == "__main__":
	main()

