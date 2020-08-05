package com.example.annotationcompiler;

import com.example.mannotation.Factory;
import com.google.auto.service.AutoService;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types typeUtils;//用来处理TypeMirror的工具类
    private Elements elementUtils;//用来处理Element的工具类
    private Filer filer;//创建文件
    private Messager messager;
    private Map<String, FactoryGroupClass> factoryClasses = new LinkedHashMap<String, FactoryGroupClass>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(Factory.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(Factory.class);
        for (Element element : elementsAnnotatedWith) {
            //检查被注解为@Factory的元素是否是一个类

            if (element.getKind() != ElementKind.CLASS){
                error(element,"Only classes can be annotated with @%s",Factory.class.getSimpleName());
                return true;
            }

            // 因为我们已经知道它是ElementKind.CLASS类型，所以可以直接强制转换
            TypeElement typeElement = (TypeElement) element;

            try {
                FactoryAnnotatedClass factoryAnnotatedClass = new FactoryAnnotatedClass(typeElement);
                if (!isValidClass(factoryAnnotatedClass)){
                    // 已经打印了错误信息，退出处理过程
                    return true;
                }

                String qualifiedSuperClassName = factoryAnnotatedClass.getQualifiedSuperClassName();
                FactoryGroupClass factoryGroupClass = factoryClasses.get(qualifiedSuperClassName);
                if (factoryGroupClass == null){
                    factoryClasses.put(qualifiedSuperClassName,new FactoryGroupClass(qualifiedSuperClassName));
                }

                factoryGroupClass.add(factoryAnnotatedClass);

                for (FactoryGroupClass value : factoryClasses.values()) {
                    value.generateCode(elementUtils,filer);
                }

            } catch (IllegalArgumentException e) {
                // @Factory.id()为空
                error(typeElement, e.getMessage());
                return true;
            }catch (Exception e){
                e.printStackTrace();
                error(typeElement,"id already exists");
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为合法的类
     *
     * 1.含public类型的构造方法
     * 2.不为抽象类
     * 3.继承自特定的类型
     *
     * @param annotatedClass
     * @return
     */
    private boolean isValidClass(FactoryAnnotatedClass annotatedClass){

        // 转换为TypeElement, 含有更多特定的方法
        TypeElement oriElement = annotatedClass.getOriElement();
        if (!oriElement.getModifiers().contains(Modifier.PUBLIC)){
            error(oriElement,"ElementType %s do not has a public construct method",oriElement.getQualifiedName().toString());
            return false;
        }

        // 检查是否是一个抽象类
        if (oriElement.getModifiers().contains(Modifier.ABSTRACT)){
            error(oriElement,"ElementType %s is abstract",oriElement.getQualifiedName().toString());
            return false;
        }
        // 检查继承关系: 必须是@Factory.type()指定的类型子类
        TypeElement superClassElement = elementUtils.getTypeElement(annotatedClass.getQualifiedSuperClassName());

        if (superClassElement.getKind() == ElementKind.INTERFACE){
            //检查接口是否被实现
            if (!oriElement.getInterfaces().contains(superClassElement.asType())){
                error(oriElement, "The class %s annotated with @%s must implement the interface %s",
                        oriElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        annotatedClass.getQualifiedSuperClassName());
                return false;
            }
        } else {
            //检查子类
            TypeElement currentElement = oriElement;
            System.out.println("current: "+currentElement.getQualifiedName());
            while (true){
                TypeMirror superclass = currentElement.getSuperclass();

                if (superclass == null){
                    break;
                }

                System.out.println("superclass: "+" "+superclass.toString() +" " + superclass.getKind());
                if (superclass.getKind() == TypeKind.NONE){
                    error(oriElement, "The class %s annotated with @%s must inherit from %s",
                            oriElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                            annotatedClass.getQualifiedSuperClassName());
                    return false;
                }

                System.out.println("test super: "+superclass.toString() +" "+annotatedClass.getQualifiedSuperClassName());
                if (superclass.toString().equals(annotatedClass.getQualifiedSuperClassName())){
                    //找到了要求的父类
                    break;
                }

                //在继承树上继续查找
                currentElement = (TypeElement) typeUtils.asElement(superclass);
            }

            //检查是否有公开的默认构造器
            for (Element enclosedElement : oriElement.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR){
                    ExecutableElement element = (ExecutableElement) enclosedElement;
                    if (element.getParameters().size() == 0 && element.getModifiers().contains(Modifier.PUBLIC)){
                        return true;
                    }
                }
            }

            // 没有找到默认构造函数
            error(oriElement, "The class %s must provide an public empty default constructor",
                    oriElement.getQualifiedName().toString());
            return false;

        }



        return false;
    }

    /**
     * 错误信息处理
     * 用来提供给三方开发者
     * 连接到出错的程序
     *
     * @param e
     * @param msg
     * @param args
     */
    private void error(Element e,String msg,Object...args){
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg,args),
                e
                );
    }
}
