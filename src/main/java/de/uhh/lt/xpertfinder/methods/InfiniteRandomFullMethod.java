package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Authorship;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfiniteRandomFullMethod implements ExpertFindingMethod {
    private static Logger logger = LoggerFactory.getLogger(InfiniteRandomFullMethod.class);

    @Override
    public String getId() {
        return "inifiniterandomfull";
    }

    @Override
    public String getName() {
        return "Infinite Random Walk - Full Graph";
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
        Map<String, Double> documentRelevance = expertTopic.getDocumentRelevance();

        int maxIterations = 500;

        // create variables
        logger.debug("Init infinite random walk full graph");
        Map<String, Double>[] pd = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pca = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pca[i] = new HashMap<>();
        }
        // init variables t = 0
        // iteration 0
        int i = 0;
        pd[0] = documentRelevance;
        for(String author : graph.getAuthors()) {
            pca[0].put(author, 0d);
        }

        // calculate random walk
        logger.debug("Calculate infinite random walk full graph");
        do {
            i++;

            for(String doc : graph.getDocs()) {
                double score = Math.exp( Math.log(lambda) + Math.log(documentRelevance.get(doc)));
                double score2 = 0;
                double score3 = 0;

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score2 = score2 + Math.exp(
                                Math.log(pca[i-1].get(authorship.getAuthor()))
                                        + Math.log(1.0d / graph.getAuthorDocumentNeighbors().get(authorship.getAuthor()).size())
                        );
                    }
                }

                if(graph.getDocumentDocumentInNeighbors().containsKey(doc)) {
                    for(String document : graph.getDocumentDocumentInNeighbors().get(doc)) {
                        score3 = score3 + Math.exp(
                                Math.log(1.0d / graph.getDocumentDocumentOutNeighbors().get(document).size())
                                        + Math.log(pd[i-1].get(document))
                        );
                    }
                }

                //score + (1 - lambda) * ((1 - mu) * score2 + mu * score3)
                score = score + Math.exp(
                        Math.log(1 - lambda) + Math.log(
                                Math.exp(
                                        Math.log(1 - md) + Math.log(score2))
                                        + Math.exp(
                                        Math.log(md) + Math.log(score3)))
                );

                pd[i].put(doc, score);
            }

            for(String author : graph.getAuthors()) {
                double score = Math.exp(Math.log(lambda) + Math.log( graph.getAuthorDocumentNeighbors().containsKey(author) ? (double) graph.getAuthorDocumentNeighbors().get(author).size() / (double) graph.getDocs().size() : 0.0d / (double) graph.getDocs().size()));
                double score2 = 0;
                double score3 = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String document : graph.getAuthorDocumentNeighbors().get(author)) {
                        score2 = score2 + Math.exp(
                                Math.log(pd[i-1].get(document))
                                        + Math.log(1.0d / graph.getDocumentAuthorNeighbors().get(document).size())
                        );
                    }
                }

                if(graph.getAuthorAuthorNeighbors().containsKey(author)) {
                    for(String auth : graph.getAuthorAuthorNeighbors().get(author)) {
                        score3 = score3 + Math.exp(
                                Math.log(1.0d / graph.getAuthorAuthorNeighbors().get(auth).size())
                                        + Math.log(pca[i-1].get(auth))
                        );
                    }
                }

                score = score + Math.exp(
                        Math.log(1 - lambda) + Math.log(
                                Math.exp(
                                        Math.log(1 - mca) + Math.log(score2))
                                        + Math.exp(
                                        Math.log(mca) + Math.log(score3))
                        )
                );

                pca[i].put(author, score);
            }

            if(i == maxIterations - 1) {
                break;
            }

        } while(!MathUtils.checkConvergence(pca[i], pca[i-1], epsilon));
        System.out.println(i + " Iterations");

        return new ExpertFindingResult(pd[i], pca[i]);
    }
}
