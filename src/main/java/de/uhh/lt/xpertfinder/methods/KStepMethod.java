package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Authorship;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KStepMethod implements ExpertFindingMethod<KStepMethod.KStepRequest> {

    public class KStepRequest extends DefaultRequest {
        private int k;

        public KStepRequest() {
            super();
            this.k = 10;
        }

        public KStepRequest(int k) {
            super();
            this.k = k;
        }

        public KStepRequest(int documents, int results, int k) {
            super(documents, results);
            this.k = k;
        }

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(KStepMethod.class);

    @Override
    public String getId() {
        return "kstep";
    }

    @Override
    public String getName() {
        return "K-Step Random Walk";
    }

    @Override
    public boolean needsCollaborations() {
        return false;
    }

    @Override
    public boolean needsCitations() {
        return false;
    }

    @Override
    public boolean needsPublications() {
        return true;
    }

    @Override
    public KStepRequest getRequestObject() {
        return new KStepRequest();
    }

    @Override
    public ExpertFindingResult findExperts(KStepRequest request, ExpertTopic expertTopic) {
        int k = request.getK();
        Graph graph = expertTopic.getGraph();
        Map<String, Double> documentRelevance = expertTopic.getDocumentRelevance();

        logger.debug("Init " + k + "-step random walk");
        // create variables
        Map<String, Double>[] pd = new Map[k];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pca = new Map[k];
        for(int i = 0; i < pd.length ; i++) {
            pca[i] = new HashMap<>();
        }
        // init variables t = 0
        pd[0] = documentRelevance;
        for(String author : graph.getAuthors()) {
            pca[0].put(author, 0d);
        }

        // calculate random walk
        logger.debug("Calculate random walk");
        for(int i = 1; i < k; i++) {
            for(String doc : graph.getDocs()) {
                double score = Math.exp(Math.log(pd[i-1].get(doc)) + Math.log(documentRelevance.get(doc)));

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score = score + Math.exp(
                                Math.log(pca[i-1].get(authorship.getAuthor()))
                                        + Math.log(1.0d / graph.getAuthorDocumentNeighbors().get(authorship.getAuthor()).size()));
                    }
                }
                pd[i].put(doc, score);
            }

            for(String author : graph.getAuthors()) {
                double score = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String doc : graph.getAuthorDocumentNeighbors().get(author)) {
                        score = score + Math.exp(
                                Math.log(1 - documentRelevance.get(doc))
                                        + Math.log(1.0d / graph.getDocumentAuthorNeighbors().get(doc).size())
                                        + Math.log(pd[i-1].get(doc)));
                    }
                }
                pca[i].put(author, score);
            }
        }

        return new ExpertFindingResult(pd[k-1], pca[k-1]);
    }
}
