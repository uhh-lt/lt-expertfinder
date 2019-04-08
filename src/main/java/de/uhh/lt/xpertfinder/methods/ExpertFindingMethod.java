package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.service.ExpertTopic;

public interface ExpertFindingMethod {

    String getId();
    String getName();
    boolean needsCollaborations();
    boolean needsCitations();
    boolean needsPublications();

    ExpertFindingResult findExperts(int k, double lambda, double epsilon, double md, double mca, ExpertTopic expertTopic);
}
