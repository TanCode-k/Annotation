package com.example.annotationcompiler;

import com.example.mannotation.Factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class FactoryAnnotatedClass {

    private TypeElement oriElement;
    private String qualifiedSuperClassName;
    private String simpleName;
    private String id;

    public FactoryAnnotatedClass(TypeElement typeElement) throws IllegalArgumentException{
        this.oriElement = typeElement;
        Factory annotation = typeElement.getAnnotation(Factory.class);
        id = annotation.id();

        if (id == null || "".equals(id)) {
            throw new IllegalArgumentException(
                    String.format("id() in @%s for class %s is null or empty! that's not allowed",
                            Factory.class.getSimpleName(), typeElement.getQualifiedName().toString())
            );
        }

        try {
            Class type = annotation.type();
            simpleName = type.getSimpleName();
            qualifiedSuperClassName = type.getCanonicalName();
        } catch (MirroredTypeException e) {
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            TypeElement element = (TypeElement) declaredType.asElement();
            simpleName = element.getSimpleName().toString();
            qualifiedSuperClassName = element.getQualifiedName().toString();

        }
    }

    /**
     * 获取被 @Factory注解的原始元素
     * @return
     */
    public TypeElement getOriElement() {
        return oriElement;
    }

    /**
     * 获取在{@link Factory#type()}定义的类合法全名
     * @return
     */
    public String getQualifiedSuperClassName() {
        return qualifiedSuperClassName;
    }

    /**
     * 获取在{@link Factory#type()}指定的类型的简单名字
     * @return
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * 获取在{@link Factory#id()}中定义的id
     * @return
     */
    public String getId() {
        return id;
    }
}
