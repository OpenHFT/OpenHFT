/*
 * Copyright 2014-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.bom.it;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class CommonsCompressSmokeTest {
    private CommonsCompressSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        assertArtifactVersion(TarArchiveEntry.class, "commons-compress", "1.28.0");
        assertArtifactVersion(SystemProperties.class, "commons-lang3", "3.18.0");
        assertArtifactVersion(IOUtils.class, "commons-io", "2.20.0");
        assertArtifactVersion(Hex.class, "commons-codec", "1.19.0");

        byte[] payload = "BOM dependency compatibility".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream archiveBytes = new ByteArrayOutputStream();
        try (TarArchiveOutputStream output = new TarArchiveOutputStream(archiveBytes)) {
            TarArchiveEntry entry = new TarArchiveEntry("entry.txt");
            entry.setSize(payload.length);
            output.putArchiveEntry(entry);
            output.write(payload);
            output.closeArchiveEntry();
        }

        try (TarArchiveInputStream input = new TarArchiveInputStream(
                new ByteArrayInputStream(archiveBytes.toByteArray()))) {
            TarArchiveEntry entry = input.getNextTarEntry();
            if (entry == null || !"entry.txt".equals(entry.getName()))
                throw new AssertionError("tar entry was not preserved");
            ByteArrayOutputStream restored = new ByteArrayOutputStream();
            byte[] buffer = new byte[128];
            int read;
            while ((read = input.read(buffer)) >= 0)
                restored.write(buffer, 0, read);
            if (!Arrays.equals(payload, restored.toByteArray()))
                throw new AssertionError("tar payload was not preserved");
        }
    }

    private static void assertArtifactVersion(Class<?> type, String artifactId, String version) {
        URL location = type.getProtectionDomain().getCodeSource().getLocation();
        String expectedJar = artifactId + "-" + version + ".jar";
        if (!location.getPath().endsWith(expectedJar))
            throw new AssertionError(type.getName() + " loaded from " + location +
                    ", expected " + expectedJar);
    }
}
