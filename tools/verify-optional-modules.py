#!/usr/bin/env python3
"""Serial, fail-visible Maven verification of reviewed standalone module roots."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--workspace", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--maven", default="mvn.cmd" if os.name == "nt" else "mvn")
    parser.add_argument("--local-repository", type=Path)
    args = parser.parse_args()
    entries = json.loads(args.manifest.read_text(encoding="utf-8"))
    workspace = args.workspace.resolve()
    args.output.mkdir(parents=True, exist_ok=True)
    results = []
    for index, entry in enumerate(entries):
        pom = (workspace / entry["pom"]).resolve()
        if not pom.is_relative_to(workspace):
            raise ValueError("POM must be inside workspace: " + str(pom))
        log = args.output / (str(index + 1).zfill(3) + ".log")
        command = [args.maven, "-B", "-ntp", "-f", str(pom),
                   "-Dsurefire.rerunFailingTestsCount=0"]
        if args.local_repository:
            command.append("-Dmaven.repo.local=" + str(args.local_repository.resolve()))
        command.extend(entry.get("arguments", ["verify"]))
        started_at = time.time()
        start = time.monotonic()
        try:
            with log.open("w", encoding="utf-8") as output:
                completed = subprocess.run(command, stdout=output, stderr=subprocess.STDOUT)
            code = completed.returncode
            error = None
        except OSError as failure:
            code, error = 127, str(failure)
        flaky_reports = []
        for report in pom.parent.rglob("TEST-*.xml"):
            if report.parent.name not in ("surefire-reports", "failsafe-reports"):
                continue
            if report.stat().st_mtime < started_at - 1:
                continue
            try:
                tree = ET.parse(report)
                if any(node.tag in ("flakyFailure", "flakyError") for node in tree.iter()):
                    flaky_reports.append(str(report))
            except (OSError, ET.ParseError) as failure:
                code = code or 1
                error = "Cannot validate test report: " + str(failure)
        if flaky_reports:
            code = code or 1
            error = "Tests passed only after retries; inspect flaky_reports"
        results.append(dict(name=entry["name"], owner=entry["owner"], pom=str(pom),
                            command=command, exit=code, error=error, log=str(log),
                            flaky_reports=flaky_reports,
                            seconds=round(time.monotonic() - start, 2)))
        (args.output / "results.json").write_text(json.dumps(results, indent=2), encoding="utf-8")
        print(entry["name"] + ": " + ("PASS" if code == 0 else "FAIL"), flush=True)
    return 1 if any(result["exit"] for result in results) else 0


if __name__ == "__main__":
    sys.exit(main())
