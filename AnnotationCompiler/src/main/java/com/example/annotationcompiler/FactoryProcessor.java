package com.example.annotationcompiler;

import com.example.mannotation.Factory;
import com.google.auto.service.AutoService;

import java.util.HashSet;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types typeUtils;//用来处理TypeMirror的工具类
    private Elements elementUtils;//用来处理Element的工具类
    private Filer filer;//创建文件
    private Messager messager;
//    private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<String, FactoryGroupedClasses>();

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
