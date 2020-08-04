package com.example.annotationcompiler;

import java.util.HashMap;
import java.util.Map;

public class FactoryGroupClass {

    private String qualfiedName;
    private Map<String, FactoryAnnotatedClass> map = new HashMap<>();

    public FactoryGroupClass(String qualfiedName) {
        this.qualfiedName = qualfiedName;
    }

    public void add(FactoryAnnotatedClass annotatedClass) {
        String id = annotatedClass.getId();
        FactoryAnnotatedClass factoryAnnotatedClass = map.get(id);

        if (factoryAnnotatedClass != null) {
            throw new IllegalArgumentException(String.format("%s already exists!", id));
        }

        map.put(id, annotatedClass);
    }
}
