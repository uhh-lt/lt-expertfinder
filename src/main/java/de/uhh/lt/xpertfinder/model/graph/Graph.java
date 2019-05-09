package de.uhh.lt.xpertfinder.model.graph;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.model.d3js.Link;
import de.uhh.lt.xpertfinder.model.d3js.Miserables;
import de.uhh.lt.xpertfinder.model.d3js.Node;
import de.uhh.lt.xpertfinder.service.HindexService;
import de.uhh.lt.xpertfinder.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public class Graph {

    private static Logger logger = LoggerFactory.getLogger(Graph.class);

    // dependencies
    private AanDao aanDao;

    // parameters
    private boolean publication;
    private boolean collaboration;
    private boolean citation;
    private List<String> topDocuments;

    // nodes
    private Set<String> authors = new HashSet<>();
    private Set<String> docs = new HashSet<>();

    // maps
    private Map<String, Long> authorIdMap = new HashMap<>();

    // edges
    private Map<String, List<Authorship>> documentAuthorNeighbors = new HashMap<>();    // document --> author
    private Map<String, List<String>> authorDocumentNeighbors = new HashMap<>();    // author --> document
    private Map<String, List<String>> documentDocumentNeighbors = new HashMap<>();  // document1 <--> document2
    private Map<String, List<Citation>> documentDocumentOutNeighbors = new HashMap<>();  // document1 --> document2: outgoing document 1
    private Map<String, List<String>> documentDocumentInNeighbors = new HashMap<>();  // document1 --> document2: incoming document 2
    private Map<String, List<Collaboration>> authorAuthorNeighbors = new HashMap<>();      // author1 <--> author2

    // stats
    private int numDocDoc;
    private int numAuthDoc;
    private int numAuthAuth;

    // info
    private HindexService hindexService;
    private Map<String, Integer> localCollaborations = new HashMap<>();
    private Map<String, Integer> documentYear = new HashMap<>();

    public Graph(AanDao aanDao, List<String> topDocuments, boolean publication, boolean collaboration, boolean citation, GraphOptions options) {
        this.aanDao = aanDao;
        this.publication = publication;
        this.collaboration = collaboration;
        this.citation = citation;
        this.topDocuments = topDocuments;

        // all top documents are nodes in the graph
        docs.addAll(topDocuments);

        // extract nodes & edges from top docs
        extractPublications();
        extractCollaborations();
        extractCitations();

        // create author <--> id map
        createAuthorIdMap();

        // get additional information from db
        pullLocalCollaborations();
        pullDocumentYear();

        // calculate additional information
        hindexService = new HindexService(aanDao, this);

        // calculate graph
        calculateCollaborationWeights(options.isCollaborationTF(), options.isCollaborationIDF());
        calculateCitationWeights(options.isCitationTF() , options.isCitationIDF());
        calculateAuthorshipWeights(options.isAuthorshipTF());
    }

    private void createAuthorIdMap() {
        List<Object[]> authorIdMapping = aanDao.findAllAuthorIds(authors);
        for(Object[] mapping : authorIdMapping) {
            authorIdMap.put((String) mapping[0], ((BigInteger)mapping[1]).longValue());
        }
    }

    private void pullLocalCollaborations() {
        if(!collaboration)
            return;

        localCollaborations.clear();

        // get authors -> author relations from top relevant authors
        logger.debug("Pull Local Collaborations");
        List<Object[]> collaborations = aanDao.findLocalCollaborations(new ArrayList<>(docs));
        for(Object[] info : collaborations) {
            localCollaborations.put(info[0] + " " + info[1], ((BigInteger) info[2]).intValue());
        }
    }

    private void pullDocumentYear() {
        logger.debug("Pull Document Year");
        List<Object[]> list = aanDao.findDocumentYear(new ArrayList<>(docs));
        for(Object[] info : list) {
            documentYear.put((String) info[0], (int) info[1]);
        }
    }

    private <T> void fillMap(Map<String, List<T>> map, Object[] data, boolean swapped) {
        Object data0 = swapped ? data[1] : data[0];
        Object data1 = swapped ? data[0] : data[1];

        if (!map.containsKey(data0)) {
            List<T> list = new ArrayList<>();
            list.add((T) data1);
            map.put((String) data0, list);
        } else {
            List<T> documents = map.get(data0);
            documents.add((T) data1);
        }
    }

    private void extractPublications() {
        if(!(publication || collaboration))
            return;

        authorDocumentNeighbors.clear();
        documentAuthorNeighbors.clear();

        // get authors -> document relations from top relevant documents
        logger.debug("Get author - document relations");
        List<Object[]> publications = aanDao.findAllPublicationsAAN(topDocuments);
        System.out.println(publications.size());
        this.numAuthDoc = publications.size();

        for(Object[] info : publications) { // info[0] = author; info[1] = document

            if (publication) {
               if (!documentAuthorNeighbors.containsKey(info[1])) {
                    List<Authorship> as = new ArrayList<>();
                    as.add(new Authorship((String) info[0]));
                    documentAuthorNeighbors.put((String) info[1], as);
                } else {
                    List<Authorship> as = documentAuthorNeighbors.get(info[1]);
                    as.add(new Authorship((String) info[0]));
                }

                fillMap(authorDocumentNeighbors, info, false);
//
//                if (!authorDocumentNeighbors.containsKey(info[0])) {
//                    List<String> documents = new ArrayList<>();
//                    documents.add((String) info[1]);
//                    authorDocumentNeighbors.put((String) info[0], documents);
//                } else {
//                    List<String> documents = authorDocumentNeighbors.get(info[0]);
//                    documents.add((String) info[1]);
//                }
                docs.add((String) info[1]);
            }
            authors.add((String) info[0]);
        }
    }

    private void extractCollaborations() {
        if(!collaboration)
            return;

        authorAuthorNeighbors.clear();

        // get authors -> author relations from top relevant authors
        logger.debug("Get author - author relations");
        List<Object[]> collaborations = aanDao.findAllCollaborationsAAN2(new ArrayList<>(authors));
        System.out.println(collaborations.size());
        this.numAuthAuth = collaborations.size();

        for(Object[] info : collaborations) { // info[0] = author1; info[1] = author2

            if(!authorAuthorNeighbors.containsKey(info[0])) {
                List<Collaboration> collaborations1 = new ArrayList<>();
                collaborations1.add(new Collaboration((String) info[1], (int) info[2]));
                authorAuthorNeighbors.put((String) info[0], collaborations1);
            } else {
                List<Collaboration> collaborations1 = authorAuthorNeighbors.get(info[0]);
                collaborations1.add(new Collaboration((String) info[1], (int) info[2]));
            }

            if(!authorAuthorNeighbors.containsKey(info[1])) {
                List<Collaboration> collaborations1 = new ArrayList<>();
                collaborations1.add(new Collaboration((String) info[0], (int) info[2]));
                authorAuthorNeighbors.put((String) info[1], collaborations1);
            } else {
                List<Collaboration> collaborations1 = authorAuthorNeighbors.get(info[1]);
                collaborations1.add(new Collaboration((String) info[0], (int) info[2]));
            }

            authors.add((String) info[0]);
            authors.add((String) info[1]);
        }
    }

    private void extractCitations() {
        if(!citation)
            return;

        documentDocumentOutNeighbors.clear();
        documentDocumentInNeighbors.clear();

        // get document -> document relations from top relevant documents
        logger.debug("Get document - document relations");
        List<Object[]> citations = aanDao.findAllCitationsAAN(topDocuments);
        System.out.println(citations.size());
        this.numDocDoc = citations.size();

        for(Object[] info : citations) { // info[0] = outgoingDocument; info[1] = incomingDocument
            if(!documentDocumentOutNeighbors.containsKey(info[0])) {
                List<Citation> documents = new ArrayList<>();
                documents.add(new Citation((String) info[1]));
                documentDocumentOutNeighbors.put((String) info[0], documents);
            } else {
                List<Citation> documents = documentDocumentOutNeighbors.get(info[0]);
                documents.add(new Citation((String) info[1]));
            }

            fillMap(documentDocumentInNeighbors, info, true);
            fillMap(documentDocumentNeighbors, info, false);
            fillMap(documentDocumentNeighbors, info, true);

//            if(!documentDocumentInNeighbors.containsKey(info[1])) {
//                List<String> documents = new ArrayList<>();
//                documents.add((String) info[0]);
//                documentDocumentInNeighbors.put((String) info[1], documents);
//            } else {
//                List<String> documents = documentDocumentInNeighbors.get(info[1]);
//                documents.add((String) info[0]);
//            }
//
//            if(!documentDocumentNeighbors.containsKey(info[0])) {
//                List<String> documents = new ArrayList<>();
//                documents.add((String) info[1]);
//                documentDocumentNeighbors.put((String) info[0], documents);
//            } else {
//                List<String> documents = documentDocumentNeighbors.get(info[0]);
//                documents.add((String) info[1]);
//            }
//
//            if(!documentDocumentNeighbors.containsKey(info[1])) {
//                List<String> documents = new ArrayList<>();
//                documents.add((String) info[0]);
//                documentDocumentNeighbors.put((String) info[1], documents);
//            } else {
//                List<String> documents = documentDocumentNeighbors.get(info[1]);
//                documents.add((String) info[0]);
//            }

            docs.add((String) info[0]);
            docs.add((String) info[1]);
        }
    }

    private void calculateAuthorshipWeights(boolean active) {
        if(!publication)
            return;

        logger.debug("Calculate authorship graph");

        for(Map.Entry<String, List<Authorship>> entry : documentAuthorNeighbors.entrySet()) {
            String doc = entry.getKey(); // TODO: document is irrelevant!

            List<Double> scores = new ArrayList<>();
            for(Authorship authorship : entry.getValue()) {
                String author = authorship.getAuthor();

                authorship.setGlobalHindex(hindexService.getGlobalHindex().getOrDefault(author, 0));
                authorship.setLocalHindex(hindexService.getLocalHindex().getOrDefault(author, 0));

                double score = 0;
                if(active) {
                    if(authorship.getLocalHindex() != 0 && authorship.getGlobalHindex() != 0) {
                        score = (double) authorship.getLocalHindex() / (double) authorship.getGlobalHindex();
                    }
                }
                authorship.setScore(score);
                scores.add(score);
            }

            for(Authorship authorship : entry.getValue()) {
                authorship.setWeight(MathUtils.softmax(authorship.getScore(), scores));
            }
        }
    }

    private void calculateCitationWeights(boolean activeTf, boolean activeIdf) {
        if(!citation)
            return;

        logger.debug("Calculate citation graph");

        for(Map.Entry<String, List<Citation>> entry : documentDocumentOutNeighbors.entrySet()) {
            String doc1 = entry.getKey();
            int year1 = documentYear.get(doc1);

            int localDistSum = 0;

            for(Citation citation : entry.getValue()) {
                String doc2 = citation.getDocument();
                int year2 = documentYear.get(doc2);

                int localDist = year1 - year2;
                localDistSum += localDist;

                citation.setYear(year2);
                citation.setGlobalDist(2018 - year2);
                citation.setLocalDist(localDist);
            }


            List<Double> scores = new ArrayList<>();
            for(Citation citation : entry.getValue()) {

                double tf = 1;
                if(localDistSum != 0) {
                    tf = ((double) localDistSum - (double) citation.getLocalDist()) / (double) localDistSum;
                }
                double idf = Math.log( (double) 53 / (double) citation.getGlobalDist());

                double score = 0;
                if(activeTf && activeIdf) {
                    score = tf * idf;
                } else if(activeTf) {
                    score = tf;
                } else if(activeIdf) {
                    score = idf;
                }

                citation.setScore(score);
                scores.add(score);
            }

            for(Citation citation : entry.getValue()) {
                citation.setWeight(MathUtils.softmax(citation.getScore(), scores));
            }
        }
    }

    private void calculateCollaborationWeights(boolean activeTf, boolean activeIdf) {
        if(!collaboration)
            return;

        logger.debug("Calculate collaboration graph");

        for(Map.Entry<String, List<Collaboration>> entry : authorAuthorNeighbors.entrySet()) {
            String author1 = entry.getKey();

            int localCollaborationSum = 0;
            Map<String, Integer> local = new HashMap<>();
            Map<String, Integer> global = new HashMap<>();

            for(Collaboration collaboration : entry.getValue()) {
                String author2 = collaboration.getAuthor();

                int colls = getLocalCollaborationCount(author1, author2);
                localCollaborationSum += colls;
                local.put(author2, colls);
                collaboration.setLocalCount(colls);
                global.put(author2, collaboration.getGlobalCount());
            }


            List<Double> scores = new ArrayList<>();
            for(Collaboration collaboration : entry.getValue()) {
                String author2 = collaboration.getAuthor();

                double tf = (double) local.get(author2) / ((double) localCollaborationSum + 1.0d);
                double idf = local.get(author2) / (double)  global.get(author2);

                double score = 0;
                if(activeTf && activeIdf) {
                    score = tf * idf;
                } else if(activeTf) {
                    score = tf;
                } else if(activeIdf) {
                    score = idf;
                }

                collaboration.setScore(score);
                scores.add(score);
            }

            for(Collaboration collaboration : entry.getValue()) {
                collaboration.setWeight(MathUtils.softmax(collaboration.getScore(), scores));
            }
        }
    }

    private void calculateAuthorWeights(Map<String, Integer>  globalhindex, Map<String, Integer>  localhindex, int sumHindex) {

        Map<String, Double> weights = new HashMap<>();

        for(String author : authors) {

            int h = localhindex.getOrDefault(author, 0);
            int gh = globalhindex.getOrDefault(author, 0);

            weights.put(author, h + gh / (double) sumHindex);

        }
    }

    public int getInDegAuthor(String author) {
        if(!authors.contains(author))
            return 0;

        return authorAuthorNeighbors.containsKey(author) ? authorAuthorNeighbors.get(author).size() : 0;
    }

    public int getOutDegAuthor(String author) {
        if(!authors.contains(author))
            return 0;

        return (authorAuthorNeighbors.containsKey(author) ? authorAuthorNeighbors.get(author).size() : 0) + (authorDocumentNeighbors.containsKey(author) ? authorDocumentNeighbors.get(author).size() : 0);
    }

    public int getInDegDocument(String document) {
        if(!docs.contains(document))
            return 0;

        return (documentDocumentInNeighbors.containsKey(document) ? documentDocumentInNeighbors.get(document).size() : 0) + (documentAuthorNeighbors.containsKey(document) ? documentAuthorNeighbors.get(document).size() : 0);
    }

    public int getOutDegDocument(String document) {
        if(!docs.contains(document))
            return 0;

        return (documentDocumentOutNeighbors.containsKey(document) ? documentDocumentOutNeighbors.get(document).size() : 0) + (documentAuthorNeighbors.containsKey(document) ? documentAuthorNeighbors.get(document).size() : 0);
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public Set<String> getDocs() {
        return docs;
    }

    public Map<String, List<Authorship>> getDocumentAuthorNeighbors() {
        return documentAuthorNeighbors;
    }

    public Map<String, List<String>> getAuthorDocumentNeighbors() {
        return authorDocumentNeighbors;
    }

    public Map<String, List<String>> getDocumentDocumentNeighbors() {
        return documentDocumentNeighbors;
    }

    public Map<String, List<Citation>> getDocumentDocumentOutNeighbors() {
        return documentDocumentOutNeighbors;
    }

    public Map<String, List<String>> getDocumentDocumentInNeighbors() {
        return documentDocumentInNeighbors;
    }

    public Map<String, List<Collaboration>> getAuthorAuthorNeighbors() {
        return authorAuthorNeighbors;
    }

    public Miserables visualizeGraph(Function<String, String> printAuthor, Function<String, String> printDocument, Function2<String, String, String> printPublication, Function2<String, String, String> printCitation, Function2<String, String, String> printCollaboration, Function2<String, String, String> printAuthorship, Function<String, Double> calcAuthorSize, Function<String, Double> calcDocumentSize, Function2<String, String, Double> calcPublicationSize, Function2<String, String, Double> calcCitationSize, Function2<String, String, Double> calcCollaborationSize, Function2<String, String, Double> calcAuthorshipSize) {
        Set<Node> nodes = new HashSet<>();
        Set<Link> links = new HashSet<>();

        // create graph
        logger.debug("Create graph nodes");
        for(String author : getAuthors()) {
            nodes.add(new Node(authorIdMap.getOrDefault(author, -1l).toString(), 1, calcAuthorSize.apply(author), printAuthor.apply(author)));
        }
        for(String document : getDocs()) {
            nodes.add(new Node(document, 2, calcDocumentSize.apply(document), printDocument.apply(document)));
        }

        logger.debug("Create graph edges");
        for (Map.Entry<String, List<String>> entry : getAuthorDocumentNeighbors().entrySet()) {
            String author = entry.getKey();
            for(String doc  : entry.getValue()) {
                Link link = new Link( "publication", authorIdMap.getOrDefault(author, -1L).toString(), doc, calcPublicationSize.apply(author, doc), printPublication.apply(author, doc));
                if(links.contains(link)) {
                    links.remove(link);
                    link.setDoubled(true);
                }
                links.add(link);
            }
        }
        for (Map.Entry<String, List<Authorship>> entry : getDocumentAuthorNeighbors().entrySet()) {
            String doc = entry.getKey();
            for(Authorship authorship  : entry.getValue()) {
                Link link = new Link( "authorship", doc, authorIdMap.getOrDefault(authorship.getAuthor(), -1L).toString(), calcAuthorshipSize.apply(doc, authorship.getAuthor()), printAuthorship.apply(doc, authorship.getAuthor()));
                if(links.contains(link)) {
                    links.remove(link);
                    link.setDoubled(true);
                }
                links.add(link);
            }
        }
        for (Map.Entry<String, List<Citation>> entry : getDocumentDocumentOutNeighbors().entrySet()) {
            String doc1 = entry.getKey();
            for(Citation citation  : entry.getValue()) {
                String doc2 = citation.getDocument();
                Link link = new Link("citation", doc1, doc2, calcCitationSize.apply(doc1, doc2), printCitation.apply(doc1, doc2));
                if(links.contains(link)) {
                    links.remove(link);
                    link.setDoubled(true);
                }
                links.add(link);
            }
        }
        for (Map.Entry<String, List<Collaboration>> entry : getAuthorAuthorNeighbors().entrySet()) {
            String author1 = entry.getKey();
            for (Collaboration collaboration1 : entry.getValue()) {
                Link link = new Link("collaboration", authorIdMap.getOrDefault(author1, -1L).toString(), authorIdMap.getOrDefault(collaboration1.getAuthor(), -1l).toString(), calcCollaborationSize.apply(author1, collaboration1.getAuthor()), printCollaboration.apply(author1, collaboration1.getAuthor()));
                if (links.contains(link)) {
                    links.remove(link);
                    link.setDoubled(true);
                }
                links.add(link);
            }
        }

        int count = 0;
        for(Link l : links) {
            count += l.isDoubled() ? 1 : 0;
        }
        System.out.println(links.size());
        System.out.println(count);
        return new Miserables(nodes, links);
    }

    public boolean isPublication() {
        return publication;
    }

    public boolean isCollaboration() {
        return collaboration;
    }

    public boolean isCitation() {
        return citation;
    }

    public int getNumDocDoc() {
        return numDocDoc;
    }

    public int getNumAuthDoc() {
        return numAuthDoc;
    }

    public int getNumAuthAuth() {
        return numAuthAuth;
    }

    public Map<String, Integer> getLocalCollaborations() {
        return localCollaborations;
    }

    public int getLocalCollaborationCount(String author1, String author2) {
        return localCollaborations.getOrDefault(author1 + " " + author2, 0);
    }

    public HindexService getHindexService() {
        return hindexService;
    }

    public Map<String, Long> getAuthorIdMap() {
        return authorIdMap;
    }

    public void setAuthorIdMap(Map<String, Long> authorIdMap) {
        this.authorIdMap = authorIdMap;
    }

    public Long getAuthorId(String author) {
        return authorIdMap.getOrDefault(author, -1l);
    }
}
