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


def createTable():
    sql0 = "DROP TABLE IF EXISTS google_authors"
    sql1 = "CREATE TABLE `google_authors` (`id` bigint(20) NOT NULL, `author_aan_id` bigint(20) DEFAULT NULL, `citations` int(11) DEFAULT NULL, `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL, `fieldsofwork` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL, `hindex` int(11) DEFAULT NULL, `i10` int(11) DEFAULT NULL, `img` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL, `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL, `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    sql2 = "ALTER TABLE `google_authors` ADD PRIMARY KEY (`id`)"
    sql3 = "ALTER TABLE `google_authors` MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT"
    mycursor.execute(sql0)
    mycursor.execute(sql1)
    mycursor.execute(sql2)
    mycursor.execute(sql3)
    mydb.commit()


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

    if not args.path:
        parser.print_help()
        return

    gsc_path = os.path.normpath(args.path)
    print(gsc_path)

    nameIdMap = createAuthorIdMap()

    print("Creating Table google_authors")
    createTable()

    print("Starting author import...")
    for entry in os.scandir(gsc_path):
        filepath = entry.path
        filename = entry.name
        if filename.endswith(".json"):
            print("Reading " + filename + "...")
            # extract id from filename
            id = os.path.splitext(filename)[0]
            with io.open(filepath, 'r', encoding="utf8") as file:
                # read file
                crawl = json.load(file)

                name = crawl['name']
                if len(name) > 255:
                    name = name[:255]

                author_id = nameIdMap.get(crawl['queryName'], "NO_ID")
                if author_id == "NO_ID":
                    print("Skipping author " + name + ", no ID available!")
                    continue

                print("Inserting author " + name)

                # Insert Google Author Information
                sql = "INSERT INTO `google_authors` (`id`, `author_aan_id`, `citations`, `description`, `fieldsofwork`, `hindex`, `i10`, `img`, `name`, `link`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
                params = (id, nameIdMap[crawl['queryName']], crawl['citations'], crawl['description'], crawl['fieldsofwork'], crawl['hindex'], crawl['i10'], crawl['img'], name, crawl['link'])
                mycursor.execute(sql, params)
                mydb.commit()

                # TODO: Insert Google Author Papers
        else:
            continue

    print("Finished importing GoogleScholar Crawl! :)")


if __name__ == "__main__":
    main()
