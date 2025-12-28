/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.util.proxy.ProxyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying Javassist bytecode manipulation library can create and modify classes.
 * <p>
 * Tests class pool operations, dynamic class creation, and proxy factory functionality
 * to ensure the Javassist dependency is correctly resolved and functional.
 */
class JavassistSmokeTest {

    @Test
    @DisplayName("Javassist ClassPool should create a new class from scratch")
    void classPoolCanCreateNewClass() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass("net.openhft.smoke.GeneratedClass");

        assertNotNull(ctClass, "ClassPool should create a new CtClass");
        assertEquals("net.openhft.smoke.GeneratedClass", ctClass.getName(),
                "Generated class should have the specified fully-qualified name");
        assertFalse(ctClass.isFrozen(), "Newly created class should not be frozen");
    }

    @Test
    @DisplayName("Javassist CtNewMethod.make should compile and add executable method to CtClass")
    void classPoolCanAddMethodToClass() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass("net.openhft.smoke.ClassWithMethod");

        CtMethod method = CtNewMethod.make(
                "public int getValue() { return 42; }",
                ctClass
        );
        ctClass.addMethod(method);

        CtMethod[] methods = ctClass.getDeclaredMethods();
        assertEquals(1, methods.length, "Class should have exactly one declared method");
        assertEquals("getValue", methods[0].getName(),
                "Method should have the correct name as defined in source");
    }

    @Test
    @DisplayName("Javassist ProxyFactory should be instantiable for dynamic proxy creation")
    void proxyFactoryCanBeInstantiated() {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(SampleSuperclass.class);

        assertNotNull(factory, "ProxyFactory should instantiate without errors");
        assertEquals(SampleSuperclass.class, factory.getSuperclass(),
                "ProxyFactory should retain configured superclass for proxy generation");
    }

    @Test
    @DisplayName("Javassist should load an existing class from the classpath")
    void classPoolCanLoadExistingClass() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("java.lang.String");

        assertNotNull(ctClass, "ClassPool should load existing String class from JDK");
        assertEquals("java.lang.String", ctClass.getName(),
                "Loaded class should have the correct canonical name");
        assertNotNull(ctClass.getSuperclass(),
                "Loaded CtClass should resolve its superclass (java.lang.Object)");
    }

    /**
     * Sample superclass used for ProxyFactory smoke test configuration.
     */
    public static class SampleSuperclass {
        /**
         * Sample method that could be overridden by a proxy.
         *
         * @return greeting text
         */
        public String greet() {
            return "Hello from superclass";
        }
    }
}
