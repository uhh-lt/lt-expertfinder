package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.finder.ExpertTopic;

public interface ExpertFindingMethod<T extends DefaultRequest> {

    String getId();
    String getName();
    boolean needsCollaborations();
    boolean needsCitations();
    boolean needsPublications();

    T getRequestObject();
    ExpertFindingResult findExperts(T request, ExpertTopic expertTopic);
}
