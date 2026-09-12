/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * CLI entry point for running MeaningfulMessage checks against source files.
 * Provides command-line options for verbose output, dry-run mode, and various output formats.
 */
public final class MeaningfulMessageCli {
    private MeaningfulMessageCli() {
    }

    public static void main(String[] args) throws Exception {
        // Propagate Checkstyle error count as the CLI process exit code.
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) throws Exception {
        CliOptions options = CliOptions.parse(args);
        if (options.parseError != null) {
            err.println(options.parseError);
            printUsage(err);
            return 2;
        }
        if (options.showHelp) {
            printUsage(out);
            return 0;
        }
        if (options.paths.isEmpty()) {
            err.println("At least one file or directory is required.");
            printUsage(err);
            return 2;
        }
        List<String> missingPaths = missingInputPaths(options.paths);
        if (!missingPaths.isEmpty()) {
            for (String missingPath : missingPaths) {
                err.println("Input path not found: " + missingPath);
            }
            return 2;
        }
        List<File> files = collectFiles(options.paths);
        if (files.isEmpty()) {
            err.println("No Java files found.");
            return 2;
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
        return errors == 0 ? 0 : 1;
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

    static List<String> missingInputPaths(List<String> paths) {
        List<String> missing = new ArrayList<>();
        for (String pathText : paths) {
            Path path = Paths.get(pathText);
            if (!Files.exists(path)) {
                missing.add(pathText);
            }
        }
        return missing;
    }

    static List<File> collectFiles(List<String> paths) throws Exception {
        Set<Path> files = new LinkedHashSet<>();
        for (String pathText : paths) {
            Path path = Paths.get(pathText);
            if (Files.isDirectory(path)) {
                try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
                    stream.filter(p -> p.toString().endsWith(".java"))
                            .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                            .forEach(p -> files.add(p.toAbsolutePath().normalize()));
                }
            } else if (path.toString().endsWith(".java")
                    && !path.getFileName().toString().equals("module-info.java")) {
                files.add(path.toAbsolutePath().normalize());
            }
        }
        List<File> collected = new ArrayList<>(files.size());
        for (Path file : files) {
            collected.add(file.toFile());
        }
        return collected;
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage: MeaningfulMessageCli [options] <files/dirs>");
        out.println("Options:");
        out.println("  --verbose           Enable verbose output");
        out.println("  --dry-run           Emit all advice and generate ranks");
        out.println("  --jsonl <path>      Write JSONL output to the given path");
        out.println("  --rank-out <path>   Write ranks to the given path (default logs/mm-advice-ranks.properties)");
        out.println("  --help              Show this help");
    }

    private static final class CliOptions {
        final boolean verbose;
        final boolean dryRun;
        final boolean showHelp;
        final String jsonl;
        final String rankOut;
        final List<String> paths;
        final String parseError;

        private CliOptions(boolean verbose, boolean dryRun, boolean showHelp,
                           String jsonl, String rankOut, List<String> paths,
                           String parseError) {
            this.verbose = verbose;
            this.dryRun = dryRun;
            this.showHelp = showHelp;
            this.jsonl = jsonl;
            this.rankOut = rankOut;
            this.paths = paths;
            this.parseError = parseError;
        }

        static CliOptions parse(String[] args) {
            boolean verbose = false;
            boolean dryRun = false;
            boolean showHelp = false;
            String jsonl = null;
            String rankOut = null;
            String parseError = null;
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
                    parseError = "--jsonl requires a path argument.";
                    break;
                }
                if ("--rank-out".equals(arg)) {
                    if (i + 1 < args.length) {
                        rankOut = args[i + 1];
                        i += 2;
                        continue;
                    }
                    parseError = "--rank-out requires a path argument.";
                    break;
                }
                if (arg.startsWith("-")) {
                    parseError = "Unknown option: " + arg;
                    break;
                }
                paths.add(arg);
                i++;
            }
            return new CliOptions(verbose, dryRun, showHelp, jsonl, rankOut, paths, parseError);
        }
    }
}
