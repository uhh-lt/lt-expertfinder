package de.uhh.lt.xpertfinder.model.d3js;

import java.util.Set;

public class Miserables {

    private Set<Node> nodes;
    private Set<Link> links;

    public Miserables(Set<Node> nodes, Set<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }
}
