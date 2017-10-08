package org.delta.distributed;

import com.squareup.javapoet.JavaFile;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@SupportedAnnotationTypes("org.delta.distributed.DistributedMethod")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DistributedMethodAnnotationProcessor extends AbstractProcessor {

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;
  private static final Integer NO_OF_DEVICES = 4;
  private static final Integer NUM = 40;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    messager.printMessage(Diagnostic.Kind.NOTE, "PROCESS");

    for(Element element: roundEnv.getElementsAnnotatedWith(DistributedMethod.class)) {
      if(element.getKind() != ElementKind.METHOD) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Can't be applied to class");
      } else {
        messager.printMessage(Diagnostic.Kind.NOTE,
            "Analyzing method annotated with DistributedMethod");

        ExecutableElement method = (ExecutableElement) element;
        Trees trees = Trees.instance(processingEnv);
        MethodTree methodTree = trees.getTree(method);

        Integer limit = 10000;
        long start = 1;

        for(int i = 0; i < NO_OF_DEVICES; i++) {
          JavaFile javaFile = JavaFileGenHelper.generateJavaFile(methodTree
                  .toString().replace("@DistributedMethod()", ""),
              methodTree.getName().toString(),
              start + "", (start + NUM/NO_OF_DEVICES - 1) + "");
          start = start + NUM/NO_OF_DEVICES;
          Path currentRelativePath = Paths.get("");
          String s = currentRelativePath.toAbsolutePath().toString();
          try {
            String writeToPath = s + "/";
            javaFile.writeTo(Paths.get(writeToPath + (i+1)));
            messager.printMessage(Diagnostic.Kind.NOTE, writeToPath);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return true;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return super.getSupportedAnnotationTypes();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return super.getSupportedSourceVersion();
  }
}
