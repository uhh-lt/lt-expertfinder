package de.uhh.lt.xpertfinder.model.graph;

@FunctionalInterface
public interface Function2<One, Two, Three> {

    public Three apply(One one, Two two);

}
