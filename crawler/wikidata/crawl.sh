#!/bin/bash

for file in authors/*; do

  # get all names in file and try to find the corresponding wikidata id and write result to file
  while read line; do
    name=$(echo "${line}")
    urlname=$(urlencode "${name}")
    echo "${name}"
    echo "${urlname}"
    wikidataid=$(curl -X GET --header 'Accept: application/json' "https://www.wikidata.org/w/api.php?format=json&action=query&list=search&srwhat=text&srsearch=${urlname}" | python -c "import json,sys; input=json.load(sys.stdin)['query']['search']; ids=['wd:'+input[x]['title'] for x in range(min(5, len(input)))]; print ' '.join(ids);")
    echo "${wikidataid}"
    echo "${line}\t${wikidataid}" >> "${file}.wiki"
  done < $file

  # go through found wikidata ids and append them to one string
  # also create a mapping fomr author id to wikidata id
  filename="${file}.wiki"
  wikistring=""
  echo "authorid,wikidataid" >> "${filename}.map.csv"
  while read line; do
    # mapping
    authorid=$(echo "${line}"| cut -f 1)
    wikidataids=$(echo "${line}"| cut -f 2)
    for wikidataid in $wikidataids
    do
        wikidataid=$(echo "${wikidataid}" | cut -c 4-)
        echo "${authorid},${wikidataid}" >> "${filename}.map.csv"
    done

    # append wikidata ids
    wikistring="${wikistring}${wikidataids} "
  done < $filename
  echo "${wikistring}"

  # create a query containing all wikidata ids
  endpoint='https://query.wikidata.org/sparql'
  query="
  SELECT ?item ?goog ?twitter ?website ?img
    (GROUP_CONCAT(DISTINCT(?label); separator = \", \") AS ?labels)
    (GROUP_CONCAT(DISTINCT(?occupation); separator = \", \") AS ?occupations)
    (GROUP_CONCAT(DISTINCT(?education); separator = \", \") AS ?educations)
    (GROUP_CONCAT(DISTINCT(?country); separator = \", \") AS ?countries)
    (GROUP_CONCAT(DISTINCT(?fieldofwork); separator = \", \") AS ?fieldofworks)
    (GROUP_CONCAT(DISTINCT(?employer); separator = \", \") AS ?employers)
    (GROUP_CONCAT(DISTINCT(?award); separator = \", \") AS ?awards)
    (GROUP_CONCAT(DISTINCT(?birthday); separator = \", \") AS ?birthdays)
  WHERE {
    VALUES ?item { ${wikistring}}

    ?item rdfs:label ?label;
          wdt:P31    wd:Q5.

    OPTIONAL { ?item wdt:P27 ?_country. }
    OPTIONAL { ?item wdt:P569 ?_birthday. }
    OPTIONAL { ?item wdt:P106 ?_occupation. }
    OPTIONAL { ?item wdt:P101 ?_fieldofwork. }
    OPTIONAL { ?item wdt:P69 ?_education. }
    OPTIONAL { ?item wdt:P108 ?_employer. }
    OPTIONAL { ?item wdt:P166 ?_award. }
    OPTIONAL { ?item wdt:P1960 ?goog. }
    OPTIONAL { ?item wdt:P2002 ?twitter. }
    OPTIONAL { ?item wdt:P856 ?website. }
    OPTIONAL { ?item wdt:P18 ?img. }
    FILTER (langMatches( lang(?label), \"EN\" ) )
    SERVICE wikibase:label {
      bd:serviceParam wikibase:language \"en\".
      ?_country rdfs:label ?country.
      ?_occupation rdfs:label ?occupation.
      ?_fieldofwork rdfs:label ?fieldofwork.
      ?_education rdfs:label ?education.
      ?_employer rdfs:label ?employer.
      ?_award rdfs:label ?award.
      ?_birthday rdfs:label ?birthday.
    }
  }
  GROUP BY ?item ?goog ?twitter ?website ?img
  "
  echo "${query}"

  # execute the query, parse the json and write result to new file
  curl -G --header 'Accept: application/json' "${endpoint}" --data-urlencode query="${query}" | jq '[.results.bindings[] | {item: .item.value, birthday: .birthdays.value, google: .goog.value, twitter: .twitter.value, website: .website.value, img: .img.value, labels: .labels.value, occupations: .occupations.value, educations: .educations.value, countries: .countries.value, fieldofworks: .fieldofworks.value, employers: .employers.value, awards: .awards.value}]' >> "${filename}.json"
  sed -i 's_http://www.wikidata.org/entity/Q_Q_g' "${filename}.json"

  # transform json to csv
  cat "${filename}.json" | jq -r '(map(keys) | add | unique) as $cols | map(. as $row | $cols | map($row[.])) as $rows | $cols, $rows[] | @csv' >> "${filename}.json.csv"

done

# merge all csv files to one whole.csv file
echo '"awards","birthday","countries","educations","employers","fieldofworks","google","img","item","labels","occupations","twitter","website"' >> whole.csv
for file2 in authors/*.wiki.json.csv; do
  sed '/"awards","birthday","countries","educations","employers","fieldofworks","google","img","item","labels","occupations","twitter","website"/d' "${file2}" >> whole.csv
done

# merge all csv files to one map.csv file
echo 'authorid,wikidataid' >> map.csv
for file in authors/*.wiki.map.csv; do
  sed '/authorid,wikidataid/d' "${file}" >> map.csv
done
