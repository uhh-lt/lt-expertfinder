package de.uhh.lt.xpertfinder.service;

import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MethodService {

    private static Logger logger = LoggerFactory.getLogger(Method.class);
    private List<ExpertFindingMethod> methods;
    private Map<String, ExpertFindingMethod> methodMap;

    @Autowired
    public MethodService(List<ExpertFindingMethod> methods) {
        this.methods = methods;
        methodMap = new HashMap<>();
        for(ExpertFindingMethod method : methods) {
            methodMap.put(method.getId(), method);
        }
        logger.debug("Initialized Method Service with " + methods.size() + " Expert Finding Methods!");
    }

    public List<ExpertFindingMethod> getAllExpertFindingMethods() {
        return this.methods;
    }

    public ExpertFindingMethod getExpertFindingMethodById(String id) {
        return this.methodMap.getOrDefault(id, null);
    }
}
