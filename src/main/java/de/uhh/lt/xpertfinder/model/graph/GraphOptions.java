package de.uhh.lt.xpertfinder.model.graph;

public class GraphOptions {


    private boolean collaborationTF;
    private boolean collaborationIDF;
    private boolean citationTF;
    private boolean citationIDF;
    private boolean authorshipTF;

    public GraphOptions() {
    }

    public GraphOptions(boolean collaborationTF, boolean collaborationIDF, boolean citationTF, boolean citationIDF, boolean authorshipTF) {
        this.collaborationTF = collaborationTF;
        this.collaborationIDF = collaborationIDF;
        this.citationTF = citationTF;
        this.citationIDF = citationIDF;
        this.authorshipTF = authorshipTF;
    }

    public boolean isCollaborationTF() {
        return collaborationTF;
    }

    public boolean isCollaborationIDF() {
        return collaborationIDF;
    }

    public boolean isCitationTF() {
        return citationTF;
    }

    public boolean isCitationIDF() {
        return citationIDF;
    }

    public boolean isAuthorshipTF() {
        return authorshipTF;
    }

    public void setCollaborationTF(boolean collaborationTF) {
        this.collaborationTF = collaborationTF;
    }

    public void setCollaborationIDF(boolean collaborationIDF) {
        this.collaborationIDF = collaborationIDF;
    }

    public void setCitationTF(boolean citationTF) {
        this.citationTF = citationTF;
    }

    public void setCitationIDF(boolean citationIDF) {
        this.citationIDF = citationIDF;
    }

    public void setAuthorshipTF(boolean authorshipTF) {
        this.authorshipTF = authorshipTF;
    }
}
