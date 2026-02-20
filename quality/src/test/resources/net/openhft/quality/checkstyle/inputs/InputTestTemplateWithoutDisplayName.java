package net.openhft.quality.checkstyle.inputs;

import org.junit.jupiter.api.TestTemplate;

class InputTestTemplateWithoutDisplayName { // violation
    @TestTemplate
    void testSomething() { // violation
    }
}
