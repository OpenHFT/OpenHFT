package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class InputTestFactoryWithoutDisplayName { // violation
    @TestFactory
    Stream<DynamicTest> dynamicTests() { // violation
        return Stream.empty();
    }
}
