package org.delta.distributed;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Arrays;

public class JavaFileGenHelper {

  public static JavaFile generateJavaFile(String method, String methodName, String... args) {

    System.out.println("Inside generateJavaFile");
    System.out.println(method);

    String argsToMethods = Arrays.stream(args).reduce((s1, s2) -> s1 + "," + s2).orElse("");

    MethodSpec main = MethodSpec.methodBuilder("main").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(void.class).addParameter(String[].class, "args")
        .addJavadoc("*/" + method.replace("\n", " ") + "/*")
        .addCode("System.out.println(" + methodName + "(" + argsToMethods + "));").build();

    TypeSpec clazz = TypeSpec.classBuilder("Main").addModifiers(Modifier.PUBLIC, Modifier.FINAL).addMethod(main)
        .build();

    return JavaFile.builder("org.delta.distributed", clazz).build();
  }
}
