package de.uhh.lt.xpertfinder.model.d3js;

public class Link {

    private String source;          // node from
    private String target;          // node to
    private double size;            // link thinkness
    private String description;     // link hover text
    private String type;            // link color || collaboration, authorship, publication, citation
    private boolean doubled;

    public Link(String type, String source, String target, double size, String description) {
        this.source = source;
        this.target = target;
        this.size = size;
        this.description = description;
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public double getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public boolean isDoubled() {
        return doubled;
    }

    public void setDoubled(boolean doubled) {
        this.doubled = doubled;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Link other = (Link) o;
        // field comparison
        return (this.getTarget().equals(other.getTarget()) && this.getSource().equals(other.getSource())) ||
               (this.getTarget().equals(other.getSource()) && this.getSource().equals(other.getTarget()));
    }

    @Override
    public int hashCode() {
        return (getTarget().charAt(0) + getSource().charAt(0));
    }
}
