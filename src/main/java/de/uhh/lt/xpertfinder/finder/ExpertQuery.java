package de.uhh.lt.xpertfinder.finder;

import de.uhh.lt.xpertfinder.model.graph.GraphOptions;

import java.io.Serializable;

public class ExpertQuery implements Serializable {

    private String topic;
    private int topDocCount;
    private int resultCount;
    private int k;
    private double lambda;
    private double epsilon;
    private double md;
    private double mca;
    private String method;
    private String method1;
    private String method2;
    private String method3;
    private String method4;

    private boolean advanced;

    private GraphOptions options;

    public ExpertQuery(String topic, int topDocCount, int resultCount, int k, double lambda, double epsilon, double md, double mca) {
        this.topic = topic;
        this.topDocCount = topDocCount;
        this.resultCount = resultCount;
        this.k = k;
        this.lambda = lambda;
        this.epsilon = epsilon;
        this.md = md;
        this.mca = mca;
        this.options = new GraphOptions();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getTopDocCount() {
        return topDocCount;
    }

    public void setTopDocCount(int topDocCount) {
        this.topDocCount = topDocCount;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean getAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public GraphOptions getOptions() {
        return options;
    }

    public void setOptions(GraphOptions options) {
        this.options = options;
    }

    public String getMethod1() {
        return method1;
    }

    public void setMethod1(String method1) {
        this.method1 = method1;
    }

    public String getMethod2() {
        return method2;
    }

    public void setMethod2(String method2) {
        this.method2 = method2;
    }

    public String getMethod3() {
        return method3;
    }

    public void setMethod3(String method3) {
        this.method3 = method3;
    }

    public String getMethod4() {
        return method4;
    }

    public void setMethod4(String method4) {
        this.method4 = method4;
    }
}
