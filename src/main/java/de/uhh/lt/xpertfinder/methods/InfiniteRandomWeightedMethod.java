package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.*;
import de.uhh.lt.xpertfinder.finder.ExpertTopic;
import de.uhh.lt.xpertfinder.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfiniteRandomWeightedMethod implements ExpertFindingMethod<InfiniteRandomWeightedMethod.InfiniteRandomWeightedRequest> {

    public class InfiniteRandomWeightedRequest extends DefaultRequest {
        private double lambda;
        private double epsilon;
        private double md;
        private double mca;
        private GraphOptions options;

        public InfiniteRandomWeightedRequest() {
        }

        public InfiniteRandomWeightedRequest(double lambda, double epsilon, double md, double mca) {
            super();
            this.lambda = lambda;
            this.epsilon = epsilon;
            this.md = md;
            this.mca = mca;
        }

        public InfiniteRandomWeightedRequest(int documents, int results, double lambda, double epsilon, double md, double mca, GraphOptions options) {
            super(documents, results);
            this.lambda = lambda;
            this.epsilon = epsilon;
            this.md = md;
            this.mca = mca;
            this.options = options;
        }

        public double getLambda() {
            return lambda;
        }

        public void setLambda(double lambda) {
            this.lambda = lambda;
        }

        public double getEpsilon() {
            return epsilon;
        }

        public void setEpsilon(double epsilon) {
            this.epsilon = epsilon;
        }

        public double getMd() {
            return md;
        }

        public void setMd(double md) {
            this.md = md;
        }

        public double getMca() {
            return mca;
        }

        public void setMca(double mca) {
            this.mca = mca;
        }

        public GraphOptions getOptions() {
            return options;
        }

        public void setOptions(GraphOptions options) {
            this.options = options;
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(InfiniteRandomWeightedMethod.class);

    private Graph graph;
    private Map<String, Double> documentRelevance;

    @Override
    public String getId() {
        return "infiniterandomweighted";
    }

    @Override
    public String getName() {
        return "Infinite Random Walk - Weighted Full Graph";
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
    public InfiniteRandomWeightedRequest getRequestObject() {
        return new InfiniteRandomWeightedRequest(1000, 25, 0.1, 0.00000008, 0.5, 0.25, new GraphOptions(true, true, true, true, true));
    }

    @Override
    public ExpertFindingResult findExperts(InfiniteRandomWeightedRequest request, ExpertTopic expertTopic) {
        double lambda = request.getLambda();
        double epsilon = request.getEpsilon();
        double md = request.getMd();
        double mca = request.getMca();
        graph = expertTopic.getGraph();
        documentRelevance = expertTopic.getDocumentRelevance();

        int maxIterations = 500;

        // create variables
        logger.debug("Init infinite random walk full weighted graph");
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
        logger.debug("Calculate infinite random walk full weighted graph");
        do {
            i++;

            for(String doc : graph.getDocs()) {
                double score = Math.exp( Math.log(lambda) + Math.log(pj(doc)));
                double score2 = 0;
                double score3 = 0;

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score2 = score2 + Math.exp(
                                Math.log(pca2d(authorship.getAuthor(), doc))
                                        + Math.log(pca[i-1].get(authorship.getAuthor()))
                        );
                    }
                }

                if(graph.getDocumentDocumentInNeighbors().containsKey(doc)) {
                    for(String document : graph.getDocumentDocumentInNeighbors().get(doc)) {
                        score3 = score3 + Math.exp(
                                Math.log(pd2d(document, doc))
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
                double score = Math.exp(Math.log(lambda) + Math.log(pjca(author)));
                double score2 = 0;
                double score3 = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String document : graph.getAuthorDocumentNeighbors().get(author)) {
                        score2 = score2 + Math.exp(
                                Math.log(pd2ca(document, author))
                                        + Math.log(pd[i-1].get(document))
                        );
                    }
                }

                if(graph.getAuthorAuthorNeighbors().containsKey(author)) {
                    for(Collaboration coll : graph.getAuthorAuthorNeighbors().get(author)) {
                        score3 = score3 + Math.exp(
                                Math.log(pca2ca(coll.getAuthor(), author))
//                                        Math.log(coll.getWeight())
                                        + Math.log(pca[i-1].get(coll.getAuthor()))
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

    // model 2 document relevance
    private double pj(String doc) {
        return documentRelevance.get(doc);
    }

    private double pca2d(String author, String doc) {
        return 1.0d / graph.getAuthorDocumentNeighbors().get(author).size();
    }

    // recency
    private double pd2d(String doc1, String doc2) {
        for (Citation citation : graph.getDocumentDocumentOutNeighbors().get(doc1)) {
            if(citation.getDocument().equals(doc2)) {
                return citation.getWeight();
            }
        }

        System.out.println("LOL WHY?!");
        return 0;
    }

    // written top docs / top docs
    private double pjca(String author) {
        return graph.getAuthorDocumentNeighbors().containsKey(author) ? (double) graph.getAuthorDocumentNeighbors().get(author).size() / (double) graph.getDocs().size() : 0.0d;
    }

    private double pd2ca(String document, String author) {
        for (Authorship authorship : graph.getDocumentAuthorNeighbors().get(document)) {
            if(authorship.getAuthor().equals(author)) {
                return authorship.getWeight();
            }
        }

        System.out.println("LOL WHY?!");
        return 0;
    }

    // local & global collaboration count
    private double pca2ca(String author1, String author2) {
        for (Collaboration collaboration : graph.getAuthorAuthorNeighbors().get(author1)) {
            if(collaboration.getAuthor().equals(author2)) {
                return collaboration.getWeight();
            }
        }

        System.out.println("LOL WHY?!");
        return 0;
    }
}
