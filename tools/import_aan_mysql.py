# encoding=utf8
import json,sys,io,os,re
from argparse import ArgumentParser
import mysql.connector
import html
import sys

mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  passwd="root",
  database="xpertfinder"
)

mycursor = mydb.cursor()

def addAuthor(name):
    splitted = name.split(", ")
    altname = ""
    if len(splitted) > 1:
        altname = splitted[1].lower().strip() + " " + splitted[0].lower().strip()
    sql = "SELECT add_authorAAN(%s, %s)"
    params = (name.lower().strip(), altname.lower().strip())
    mycursor.execute(sql, params)
    myresult = mycursor.fetchall()
    mydb.commit()


def addDocument(file, title, venue, year):
    sql = "SELECT add_documentAAN(%s, %s, %s, %s)"
    params = (file.strip(), title.strip(), venue.strip(), year)
    mycursor.execute(sql, params)
    myresult = mycursor.fetchall()
    mydb.commit()


def addCitation(outgoing, incoming):
    sql = "SELECT add_citationAAN(%s, %s)"
    params = (outgoing.strip(), incoming.strip())
    mycursor.execute(sql, params)
    myresult = mycursor.fetchall()
    mydb.commit()


def addPublication(author, document):
    sql = "SELECT add_publicationAAN(%s, %s)"
    params = (author.lower().strip(), document.strip())
    mycursor.execute(sql, params)
    myresult = mycursor.fetchall()
    mydb.commit()


def addCollaboration(author1, author2):
    sql = "SELECT add_collaborationAAN2(%s, %s)"
    params = (author1.lower().strip(), author2.lower().strip())
    mycursor.execute(sql, params)
    myresult = mycursor.fetchall()
    mydb.commit()


def main():
    parser = ArgumentParser()
    parser.add_argument("-a", "--aan", dest="aan", help="path to AAN", metavar="AAN")

    args = parser.parse_args()

    if not args.aan:
        parser.print_help()
        return

    aan_path = os.path.normpath(args.aan)
    citations = os.path.normpath("release/2014/networks/paper-citation-network.txt")
    metadata = os.path.normpath("release/2014/acl-metadata.txt")
    citations_path = os.path.normpath(aan_path+"/"+citations)
    metadata_path = os.path.normpath(aan_path+"/"+metadata)

    id = ""
    author = ""
    title = ""
    venue = ""
    year = 0

    with io.open(metadata_path, 'r', encoding="utf-8", errors='replace') as file:
        for l in file:

            line = html.unescape(l)
            print(line)

            matchEmpty = re.match(r'^\s*$', line)
            if matchEmpty:
                print("id:"+id+" author: "+author+" title: "+title+" venue: "+venue+" year: "+year)

                addDocument(id, title, venue, year) # add the document

                authors = author.split(";")
                firstAuthor = authors[0]
                addAuthor(firstAuthor) # add first author
                addPublication(firstAuthor, id) # add publication for first author
                for a in authors[1:]:
                    addAuthor(a) # add other authors
                    addPublication(a, id) # add publication for other authors

                count = 1
                for a in authors:
                    for b in authors[count:]:
                        addCollaboration(a, b) # add collaborations between all authors
                    count = count + 1


                id = ""
                author = ""
                title = ""
                venue = ""
                year = 0
            else:
                matchData = re.match(r'^(.*) = {(.*)}$', line)
                if matchData:
                    type = matchData.group(1)
                    value = matchData.group(2)

                    if type == "id":
                        id = value
                    elif type == "author":
                        author = value
                    elif type == "year":
                        year = value
                    elif type == "venue":
                        venue = value
                    elif type == "title":
                        title = value


    with io.open(citations_path, 'r', encoding="utf-8", errors='replace') as file:
        for line in file:
            outgoing, incoming = line.split(" ==> ")
            print("Inserting Citation: "+outgoing.strip()+" ==> "+incoming.strip())
            addCitation(outgoing.strip(), incoming.strip())


if __name__ == "__main__":
    main()

