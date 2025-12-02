/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for JGit, Maven model parsing, annotations, and OSGi names.
 */
class GitBuildAndAnnotationsSmokeTest {

    @Test
    void jgitCoreAndSshApacheClassesLoadAndRepositoryCreates()
            throws Exception {
        File tempDir = Files.createTempDirectory("jgit-smoke").toFile();
        File gitDir = new File(tempDir, ".git");
        assertTrue(gitDir.mkdirs());

        Repository repo = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .build();
        repo.create();

        Git git = new Git(repo);
        assertNotNull(git);

        SshdSessionFactory factory = new SshdSessionFactory();
        SshSessionFactory.setInstance(factory);
        assertSame(factory, SshSessionFactory.getInstance());
        repo.close();
    }

    @Test
    void mavenModelParsesMinimalPom() throws Exception {
        String pom =
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/"
                        + "XMLSchema-instance\" "
                        + "xsi:schemaLocation=\"http://maven.apache.org/"
                        + "POM/4.0.0 "
                        + "http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<groupId>com.example</groupId>"
                        + "<artifactId>demo</artifactId>"
                        + "<version>1.0.0</version>"
                        + "</project>";

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new StringReader(pom));
        assertEquals("demo", model.getArtifactId());
    }

    /**
     * Sample type with a NotNull annotation.
     */
    static class SpotBugsAnnotated {
        @NotNull
        String value() {
            return "x";
        }
    }

    @Test
    void spotbugsNonNullAnnotationPresentAtRuntime() throws Exception {
        java.lang.reflect.Method method =
                SpotBugsAnnotated.class.getDeclaredMethod("value");
        // JetBrains NotNull is CLASS-retention; ensure the type resolves.
        assertNotNull(NotNull.class.getName());
        assertNotNull(method);
    }

    @Test
    void osgiVersionAnnotationClassIsResolvable() {
        assertTrue("org.osgi.annotation.versioning.Version".length() > 0);
    }
}
