/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Modifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test verifying JavaPoet can generate simple Java source files.
 */
@DisplayName("JavaPoetSmokeTest")
class JavaPoetSmokeTest {

    @Test
    @DisplayName("JavaPoet should generate a simple HelloWorld class")
    void javapoetCanGenerateSimpleHelloWorldClass() {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class,
                        "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld",
                        helloWorld)
                .build();

        String source = javaFile.toString();
        assertTrue(source.contains("class HelloWorld"),
                source + " should contain class HelloWorld");
        assertTrue(source.contains("Hello, JavaPoet!"),
                source + " should contain Hello, JavaPoet!");
    }
}
