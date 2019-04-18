import json,sys,io,os,re
from pprint import pprint
from argparse import ArgumentParser
import mysql.connector

mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  passwd="root",
  database="xpertfinder"
)

mycursor = mydb.cursor()


def createAuthorIdMap():
    resultmap = {}

    sql = "SELECT id, alt_name FROM authors_aan"
    mycursor.execute(sql)
    myresult = mycursor.fetchall()

    for row in myresult:
        id = row[0]
        name = row[1]
        name = str(name).strip()
        resultmap[name] = id

    return resultmap

def main():
    parser = ArgumentParser()
    parser.add_argument("-p", "--path", dest="path", help="path to authors directory containing crawled .json files", metavar="PATH")
    args = parser.parse_args()

    gsc_path = os.path.normpath(args.path)
    print(gsc_path)

    nameIdMap = createAuthorIdMap()

    for entry in os.scandir(gsc_path):
        filepath = entry.path
        filename = entry.name
        if filename.endswith(".json"):
            print("Indexing " + filename)
            # extract id from filename
            id = os.path.splitext(filename)[0]
            with io.open(filepath, 'r', encoding="utf8") as file:
                # read file
                crawl = json.load(file)

                # create json object

                pprint(crawl)

                # print(id)
                # print(crawl['description'])

                name = crawl['name']
                if len(name) > 255:
                    name = name[:255]

                # Insert Google Author Information
                sql = "INSERT INTO `google_authors` (`id`, `author_aan_id`, `citations`, `description`, `fieldsofwork`, `hindex`, `i20`, `img`, `name`, `link`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s);"
                params = (id, nameIdMap[crawl['queryName']], crawl['citations'], crawl['description'], crawl['fieldsofwork'], crawl['hindex'], crawl['i10'], crawl['img'], name, crawl['link'])
                mycursor.execute(sql, params)
                mydb.commit()

                # TODO: Insert Google Author Papers
        else:
            continue


if __name__ == "__main__":
    main()
