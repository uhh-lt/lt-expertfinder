package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Authorship;
import de.uhh.lt.xpertfinder.model.graph.Collaboration;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PageRankMethod implements ExpertFindingMethod{
    private static Logger logger = LoggerFactory.getLogger(PageRankMethod.class);

    @Override
    public String getId() {
        return "pagerank";
    }

    @Override
    public String getName() {
        return "Page Rank";
    }

    @Override
    public boolean needsCollaborations() {
        return true;
    }

    @Override
    public boolean needsCitations() {
        return true;
    }

    @Override
    public boolean needsPublications() {
        return true;
    }

    @Override
    public ExpertFindingResult findExperts(int k, double lambda, double epsilon, double md, double mca, ExpertTopic expertTopic) {
        Graph graph = expertTopic.getGraph();
        int maxIterations = 500;

        // create variables
        logger.debug("Init page rank");

        Map<String, Double>[] pd = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pa = new Map[maxIterations];
        for(int i = 0; i < pa.length ; i++) {
            pa[i] = new HashMap<>();
        }

        Set<String> docs = graph.getDocs();
        Set<String> authors = graph.getAuthors();
        int n = docs.size() + authors.size();

        // iteration 0
        int i = 0;
        for(String doc : docs) {
            pd[i].put(doc, 1.0d / n);
        }
        for(String author : authors) {
            pa[i].put(author, 1.0d / n);
        }

        double d = lambda;

        do {
            i++;

            for(String doc : docs) {

                double score = (1 - d) / n;
                double sum1 = 0;
                double sum2 = 0;

                if(graph.getDocumentDocumentInNeighbors().containsKey(doc)) {
                    for(String inc_doc : graph.getDocumentDocumentInNeighbors().get(doc)) {
                        sum1 += pd[i-1].get(inc_doc) / graph.getOutDegDocument(inc_doc);
                    }
                }

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        String inc_author = authorship.getAuthor();
                        sum2 += pa[i-1].get(inc_author) / graph.getOutDegAuthor(inc_author);
                    }
                }

                pd[i].put(doc, score + d * (sum1 + sum2));
            }

            for(String author : authors) {

                double score = (1 - d) / n;
                double sum1 = 0;
                double sum2 = 0;

                if(graph.getAuthorAuthorNeighbors2().containsKey(author)) {
                    for(Collaboration collaboration : graph.getAuthorAuthorNeighbors2().get(author)) {
                        String inc_author = collaboration.getAuthor();
                        sum1 += pa[i-1].get(inc_author)  / graph.getOutDegAuthor(inc_author);
                    }
                }

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String inc_doc : graph.getAuthorDocumentNeighbors().get(author)) {
                        sum2 = pd[i-1].get(inc_doc) / graph.getOutDegDocument(inc_doc);
                    }
                }

                pa[i].put(author, score + d * (sum1 +sum2));
            }

            if(i == maxIterations - 1)
                break;

        } while(!MathUtils.checkConvergence(pa[i], pa[i-1], epsilon));

        System.out.println("Iterations: " + i);

        return new ExpertFindingResult(pd[i], pa[i]);
    }
}
