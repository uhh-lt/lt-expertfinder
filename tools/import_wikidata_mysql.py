import json,sys,io,os,re
from pprint import pprint
from argparse import ArgumentParser
import mysql.connector
import csv

mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  passwd="root",
  database="xpertfinder"
)

mycursor = mydb.cursor()


def createTable():
    sql0 = "DROP TABLE IF EXISTS wikidata"
    sql1 = "CREATE TABLE `wikidata` ( `authorid` bigint(20) DEFAULT NULL, `item` varchar(9) DEFAULT NULL, `labels` varchar(255) DEFAULT NULL, `awards` varchar(1001) DEFAULT NULL, `birthday` varchar(255) DEFAULT NULL, `countries` varchar(106) DEFAULT NULL, `educations` varchar(253) DEFAULT NULL, `employers` varchar(284) DEFAULT NULL, `fieldofworks` varchar(255) DEFAULT NULL, `google` varchar(12) DEFAULT NULL, `img` varchar(387) DEFAULT NULL, `occupations` varchar(263) DEFAULT NULL, `twitter` varchar(15) DEFAULT NULL, `website` varchar(118) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8"
    sql2 = "ALTER TABLE `wikidata` ADD KEY `item` (`item`)"
    mycursor.execute(sql0)
    mycursor.execute(sql1)
    mycursor.execute(sql2)
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
    parser.add_argument("-c", "--csv", dest="csv", help="path to csv", metavar="CSV")
    parser.add_argument("-m", "--map", dest="map", help="path to map", metavar="MAP")
    args = parser.parse_args()
    csv_path = os.path.normpath(args.csv)
    map_path = os.path.normpath(args.map)
    print(csv_path)
    print(map_path)
    nameIdMap = createAuthorIdMap()
    print(nameIdMap["akio fujiyoshi"])

    createTable()

    # create wikidata id to author name mapping
    wikiNameMap = {}
    with open(map_path, mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        line_count = 0
        for row in csv_reader:
            if line_count == 0:
                print("".join(row))
                line_count += 1
            line_count += 1
            wikiNameMap[row['wikidataid']] = row['authorid']

    # import wikidata
    with open(csv_path, mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        line_count = 0
        for row in csv_reader:
            if line_count == 0:
                print("".join(row))
                line_count += 1
            line_count += 1

            wikidataid = row['item']
            authorname = wikiNameMap[wikidataid]
            authorid = nameIdMap[authorname]
            print(wikidataid+"->"+authorname+"->"+str(authorid))

            awards = row['awards']
            if len(awards) > 1000:
                awards = awards[:1000]

            sql = "INSERT INTO `wikidata` (`authorid`, `item`, `labels`, `awards`, `birthday`, `countries`, `educations`, `employers`, `fieldofworks`, `google`, `img`, `occupations`, `twitter`, `website`) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
            params = (authorid, row['item'], row['labels'], awards, row['birthday'], row['countries'], row['educations'], row['employers'], row['fieldofworks'], row['google'], row['img'], row['occupations'], row['twitter'], row['website'])
            mycursor.execute(sql, params)
            mydb.commit()



if __name__ == "__main__":
    main()
