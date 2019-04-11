package de.uhh.lt.xpertfinder.finder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.model.graph.GraphOptions;
import de.uhh.lt.xpertfinder.service.MethodService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpertQuery implements Serializable {

    private String topic;
    private String[] method;
    private List<Map<String,String>> methodParamMap;
    private List<Map<String,String>> methodNameMap;
    private List<Map<String,Integer>> methodParamLengthMap;

    private boolean advanced;

    private GraphOptions options;

    public ExpertQuery(String topic, String[] method, MethodService methodService) {
        this.topic = topic;
        this.method = method;
        this.options = new GraphOptions();
        initMethods(methodService);
    }

    private void initMethods(MethodService methodService) {
        this.methodParamMap = new ArrayList<>();
        this.methodParamLengthMap = new ArrayList<>();
        this.methodNameMap = new ArrayList<>();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<ExpertFindingMethod> allExpertFindingMethods = methodService.getAllExpertFindingMethods();
        for(int i = 0; i < 4; i++) {

            Map<String, String> methodParamMap = new HashMap<>();
            Map<String, Integer> methodParamLengthMap = new HashMap<>();
            Map<String, String> methodNameMap = new HashMap<>();

            for(ExpertFindingMethod efmethod : allExpertFindingMethods) {
                String params = gson.toJson(efmethod.getRequestObject());
                int length = countLines(params);
                methodParamMap.put(efmethod.getId(), params);
                methodParamLengthMap.put(efmethod.getId(), length);
                methodNameMap.put(efmethod.getId(), efmethod.getName());
            }
            this.methodParamMap.add(methodParamMap);
            this.methodParamLengthMap.add(methodParamLengthMap);
            this.methodNameMap.add(methodNameMap);
        }
    }

    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String[] getMethod() {
        return method;
    }

    public void setMethod(String[] method) {
        this.method = method;
    }

    public List<Map<String, String>> getMethodParamMap() {
        return methodParamMap;
    }

    public void setMethodParamMap(List<Map<String, String>> methodParamMap) {
        this.methodParamMap = methodParamMap;
    }

    public List<Map<String, Integer>> getMethodParamLengthMap() {
        return methodParamLengthMap;
    }

    public void setMethodParamLengthMap(List<Map<String, Integer>> methodParamLengthMap) {
        this.methodParamLengthMap = methodParamLengthMap;
    }

    public boolean isAdvanced() {
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

    public List<Map<String, String>> getMethodNameMap() {
        return methodNameMap;
    }

    public void setMethodNameMap(List<Map<String, String>> methodNameMap) {
        this.methodNameMap = methodNameMap;
    }
}
