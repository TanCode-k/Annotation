package com.example.annotationcompiler;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

public class FactoryGroupClass {
    private String SUFFIX = "Factory";

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

    public void generateCode(Elements elements,Filer filer){
        TypeElement typeElement = elements.getTypeElement(qualfiedName);
        String fileName = typeElement.getSimpleName() + SUFFIX;
        try {
            JavaFileObject sourceFile = filer.createSourceFile(fileName);
            Writer writer = sourceFile.openWriter();
            //todo write file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
