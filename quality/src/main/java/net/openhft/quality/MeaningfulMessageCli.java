/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CLI entry point for running MeaningfulMessage checks against source files.
 * Provides command-line options for verbose output, dry-run mode, and various output formats.
 */
@SuppressWarnings({"MMTooShort", "MMMissingMessage"})
public final class MeaningfulMessageCli {
    private MeaningfulMessageCli() {
    }

    public static void main(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        if (options.showHelp || options.paths.isEmpty()) {
            printUsage();
            // Exit early when help requested or no paths provided
            System.exit(options.paths.isEmpty() ? 2 : 0);
        }
        List<File> files = collectFiles(options.paths);
        if (files.isEmpty()) {
            // Exit with error when no Java files found in paths
            System.err.println("No Java files found.");
            System.exit(2);
        }
        Configuration config = buildConfiguration(options);
        Checker checker = new Checker();
        int errors;
        try {
            checker.setModuleClassLoader(MeaningfulMessageCli.class.getClassLoader());
            checker.configure(config);
            errors = checker.process(files);
        } finally {
            checker.destroy();
        }
        // Exit with success only when no errors found
        System.exit(errors == 0 ? 0 : 1);
    }

    private static Configuration buildConfiguration(CliOptions options) {
        final DefaultConfiguration mm = new DefaultConfiguration(MeaningfulMessageCheck.class.getName());
        if (options.verbose) {
            mm.addProperty("verbose", "true");
        }
        if (options.dryRun) {
            mm.addProperty("dryRun", "true");
        }
        if (options.jsonl != null) {
            mm.addProperty("jsonl", options.jsonl);
        }
        if (options.rankOut != null) {
            mm.addProperty("rankOut", options.rankOut);
        }
        final DefaultConfiguration treeWalker = new DefaultConfiguration("TreeWalker");
        treeWalker.addChild(mm);
        final DefaultConfiguration checkerConfig = new DefaultConfiguration("Checker");
        checkerConfig.addChild(treeWalker);
        return checkerConfig;
    }

    private static List<File> collectFiles(List<String> paths) throws Exception {
        List<File> files = new ArrayList<>();
        for (String pathText : paths) {
            Path path = Paths.get(pathText);
            if (!Files.exists(path)) {
                continue;
            }
            if (Files.isDirectory(path)) {
                try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
                    stream.filter(p -> p.toString().endsWith(".java"))
                            .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                            .forEach(p -> files.add(p.toFile()));
                }
            } else if (path.toString().endsWith(".java")
                    && !path.getFileName().toString().equals("module-info.java")) {
                files.add(path.toFile());
            }
        }
        return files;
    }

    private static void printUsage() {
        System.out.println("Usage: MeaningfulMessageCli [options] <files/dirs>");
        System.out.println("Options:");
        System.out.println("  --verbose           Enable verbose output");
        System.out.println("  --dry-run           Emit all advice and generate ranks");
        System.out.println("  --jsonl <path>      Write JSONL output to the given path");
        System.out.println("  --rank-out <path>   Write ranks to the given path (default logs/mm-advice-ranks.properties)");
        System.out.println("  --help              Show this help");
    }

    private static final class CliOptions {
        final boolean verbose;
        final boolean dryRun;
        final boolean showHelp;
        final String jsonl;
        final String rankOut;
        final List<String> paths;

        private CliOptions(boolean verbose, boolean dryRun, boolean showHelp,
                           String jsonl, String rankOut, List<String> paths) {
            this.verbose = verbose;
            this.dryRun = dryRun;
            this.showHelp = showHelp;
            this.jsonl = jsonl;
            this.rankOut = rankOut;
            this.paths = paths;
        }

        static CliOptions parse(String[] args) {
            boolean verbose = false;
            boolean dryRun = false;
            boolean showHelp = false;
            String jsonl = null;
            String rankOut = null;
            List<String> paths = new ArrayList<>();
            int i = 0;
            while (i < args.length) {
                String arg = args[i];
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    showHelp = true;
                    i++;
                    continue;
                }
                if ("--verbose".equals(arg)) {
                    verbose = true;
                    i++;
                    continue;
                }
                if ("--dry-run".equals(arg)) {
                    dryRun = true;
                    i++;
                    continue;
                }
                if ("--jsonl".equals(arg)) {
                    if (i + 1 < args.length) {
                        jsonl = args[i + 1];
                        i += 2;
                        continue;
                    }
                    showHelp = true;
                    break;
                }
                if ("--rank-out".equals(arg)) {
                    if (i + 1 < args.length) {
                        rankOut = args[i + 1];
                        i += 2;
                        continue;
                    }
                    showHelp = true;
                    break;
                }
                paths.add(arg);
                i++;
            }
            return new CliOptions(verbose, dryRun, showHelp, jsonl, rankOut, paths);
        }
    }
}
