package de.uhh.lt.xpertfinder.model.d3js;

public class Node {

    private String id;
    private int group;
    private double size;
    private String description;

    public Node(String id, int group, double size, String description) {
        this.id = id;
        this.group = group;
        this.size = size;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
