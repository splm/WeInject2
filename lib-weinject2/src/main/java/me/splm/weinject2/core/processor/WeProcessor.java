package me.splm.weinject2.core.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import me.splm.annotation.WeInject;


@AutoService(Processor.class)
public class WeProcessor extends AbstractProcessor {
    public static final String TARGET_PACKAGE_NAME="me.splm.gen.auto";
    private Set<String> supportedAnnotationTypes = new HashSet<>();
    private Map<String,Member> types=new HashMap<>();
    private Member member=new Member();
    /**
     * collection of setters
     */
    private Set<MethodSpec> set_methodSpecs=new LinkedHashSet<>();
    /**
     * collection of inner class getters
     */
    private Set<MethodSpec> get_InnerMethodSpecs=new LinkedHashSet<>();
    /**
     * collection of inner class setters
     */
    private Set<MethodSpec> set_InnerMethodSpecs=new LinkedHashSet<>();
    /**
     * all of fields will appear here.
     */
    private Set<FieldSpec> fieldSpecs=new LinkedHashSet<>();
    /**
     * name of class
     */
    private String cName;
    /**
     * abssolute path of class
     */
    private String absName;
    /**
     * packagename of class
     */
    private String pName;
    @Override
    public synchronized void init(ProcessingEnvironment arg0) {
        super.init(arg0);
        supportedAnnotationTypes.add(WeInject.class.getCanonicalName());
    }
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment renv) {
        Messager msger=processingEnv.getMessager();
        msger.printMessage(Diagnostic.Kind.NOTE,"start");
        for(Element element:renv.getElementsAnnotatedWith(WeInject.class)){
            if(element.getKind()== ElementKind.METHOD){
                msger.printMessage(Diagnostic.Kind.NOTE, "This annotation is not apply for Method!");
                return false;
            }

            if(element.getKind()==ElementKind.FIELD){
                //FIELD
                msger.printMessage(Diagnostic.Kind.NOTE, "Target is Field!");
                String name=element.toString();
                msger.printMessage(Diagnostic.Kind.NOTE, name);//name=testString
                member.addFields(name);
            }
            if(element.getKind()==ElementKind.CLASS){
                //Class
                msger.printMessage(Diagnostic.Kind.NOTE, "Target is Class!");
                cName=element.getSimpleName().toString();
                absName=element.toString();
                int lastIndex=absName.lastIndexOf(".");
                pName=absName.subSequence(0,lastIndex).toString();
            }
        }
        try {
            genTargetClazz(msger);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void genTargetClazz(Messager msger) throws ClassNotFoundException{
        try {
            ClassName clazzName = ClassName.get("me.splm.gen.auto","We"+cName);
            /**
             * Just like:private static final String TAG = "WeSecActivity";
             */
            FieldSpec tag=FieldSpec.builder(String.class, "TAG", Modifier.PRIVATE,Modifier.STATIC,Modifier.FINAL)
                    .addJavadoc("Intent object will find data with it!")
                    .initializer("$S","We"+cName)
                    .build();
            /**
             * like: private static WeSecActivity instance;
             */
            FieldSpec instance=FieldSpec.builder(clazzName, "instance", Modifier.PRIVATE,Modifier.STATIC)
                    .addJavadoc("create the singleton of @{link "+ clazzName +"} ")
                    .build();
            TypeName classOfBridgeData=ClassName.get(TARGET_PACKAGE_NAME+".We"+cName,"BridgeDataClass");
            /**
             * android's activity
             */
            TypeName activityOfMethod=ClassName.get("android.app","Activity");
            /**
             * android's intent
             */
            TypeName intentOfMethod=ClassName.get("android.content","Intent");
            TypeName genClass=ClassName.get(pName,cName);//auto gen class's path
            /**
             * like: private static BridgeDataClass bridgeData;
             */
            FieldSpec bridgeData=FieldSpec.builder(classOfBridgeData, "bridgeData", Modifier.PRIVATE,Modifier.STATIC)
                    .build();
            ParameterSpec paramActivityOfInject=ParameterSpec.builder(genClass,"activity").build();
            MethodSpec.Builder injectMethodBuilder=genInjectMethodBuilder(paramActivityOfInject,classOfBridgeData);
            Set<String> fields=member.getFields();
            if(!fields.isEmpty()){
                for(String s:fields){
                    String newStr=s.substring(0, 1).toUpperCase()+s.replaceFirst("\\w","");
                    FieldSpec fieldSpec=FieldSpec.builder(String.class,s,Modifier.PUBLIC).build();
                    fieldSpecs.add(fieldSpec);
                    MethodSpec set_methodSpec=MethodSpec.methodBuilder("set"+newStr)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(clazzName)
                            .addParameter(String.class,s)
                            .addStatement("this.$N.set"+newStr+"($N)",bridgeData,s)
                            .addStatement("return this")
                            .build();
                    set_methodSpecs.add(set_methodSpec);

                    CodeBlock codeBlock=CodeBlock.builder().addStatement("$N."+s+"=data.get"+newStr+"()",paramActivityOfInject).build();
                    injectMethodBuilder.addCode(codeBlock);
                    //create getter/setter method for the innerClass's fields
                    MethodSpec get_innerMethodSpec=MethodSpec.methodBuilder("get"+newStr)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(String.class)
                            .addStatement("return $N",s)
                            .build();
                    get_InnerMethodSpecs.add(get_innerMethodSpec);

                    MethodSpec set_innerMethodSpec=MethodSpec.methodBuilder("set"+newStr)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(String.class,s)
                            .addStatement("this.$N=$N",s,s)
                            .build();
                    set_InnerMethodSpecs.add(set_innerMethodSpec);
                }
            }
            MethodSpec inject=injectMethodBuilder.build();
            MethodSpec constuctor=MethodSpec.constructorBuilder()
                    .addJavadoc("Could not build object by new keyword!")
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            MethodSpec getInstance=MethodSpec.methodBuilder("getIntance")
                    .addJavadoc("build a normal singleton")
                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                    .returns(clazzName)
                    .beginControlFlow("if($N==null)",instance)
                    .addStatement("$N=new $T()", instance,clazzName)
                    .endControlFlow()
                    .addStatement("$N=new $T()",bridgeData,classOfBridgeData)
                    .addStatement("return $N", instance)
                    .build();
            MethodSpec start=MethodSpec.methodBuilder("start")
                    .addJavadoc("Will open target Activity")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(activityOfMethod,"activity")
                    .addStatement("$T intent=new $T()",intentOfMethod,intentOfMethod)
                    .addStatement("intent.setClass($N, $N.class)","activity",absName)
                    .addStatement("intent.putExtra($N, $N)",tag,bridgeData)
                    .addStatement("$N.startActivity(intent)","activity")
                    .build();
            MethodSpec getArguments=MethodSpec.methodBuilder("getArgument")
                    .addModifiers(Modifier.PRIVATE,Modifier.STATIC)
                    .addParameter(intentOfMethod,"intent")
                    .returns(classOfBridgeData)
                    .beginControlFlow("if($N==null)",bridgeData)
                    .addStatement("return new $T()",classOfBridgeData)
                    .endControlFlow()
                    .addStatement("return ($T)intent.getSerializableExtra($N)",classOfBridgeData,tag)
                    .build();
            msger.printMessage(Diagnostic.Kind.NOTE,absName);
            TypeSpec bridgeDataClazz=TypeSpec.classBuilder("BridgeDataClass")
                    .addModifiers(Modifier.PRIVATE,Modifier.STATIC)
                    .addSuperinterface(Serializable.class)
                    .addMethods(set_InnerMethodSpecs)
                    .addMethods(get_InnerMethodSpecs)
                    .addFields(fieldSpecs)
                    .build();
            TypeSpec targetClazz=TypeSpec.classBuilder("We"+cName)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(tag)
                    .addField(instance)
                    .addField(bridgeData)
                    .addFields(fieldSpecs)
                    .addMethod(constuctor)
                    .addMethod(getInstance)
                    .addMethod(start)
                    .addMethod(inject)
                    .addMethod(getArguments)
                    .addMethods(set_methodSpecs)
                    .addType(bridgeDataClazz)
                    .build();
            JavaFile javaFile=JavaFile.builder(TARGET_PACKAGE_NAME,targetClazz).build();
            javaFile.writeTo(processingEnv.getFiler());
            msger.printMessage(Diagnostic.Kind.NOTE,"complete!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec.Builder genInjectMethodBuilder(ParameterSpec parm,TypeName field){
        MethodSpec.Builder builder=MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .returns(void.class)
                .addParameter(parm)
                .beginControlFlow("if($N==null)",parm)
                .addStatement("return")
                .endControlFlow()
                .addStatement("$T data=getArgument($N.getIntent())",field,parm);
        return builder;
    }

}
