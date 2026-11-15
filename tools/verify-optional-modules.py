#!/usr/bin/env python3
"""Serial, fail-visible Maven verification of reviewed standalone module roots."""
import argparse
import json
import math
import os
from pathlib import Path
import signal
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


def stop_owned_process(process):
    """Stop only this invocation's process tree, including Maven's test JVMs."""
    if os.name == "nt":
        subprocess.run(["taskkill", "/PID", str(process.pid), "/T", "/F"],
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=10)
    else:
        for sig in (signal.SIGTERM, signal.SIGKILL):
            try:
                os.killpg(process.pid, sig)
            except ProcessLookupError:
                break
            if sig == signal.SIGTERM:
                try:
                    process.wait(timeout=2)
                except subprocess.TimeoutExpired:
                    pass
    process.wait(timeout=5)


def command_output(command, cwd=None):
    try:
        result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, timeout=10)
        return (result.stdout + result.stderr).strip() if result.returncode == 0 else None
    except (OSError, subprocess.TimeoutExpired):
        return None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--workspace", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--maven", default="mvn.cmd" if os.name == "nt" else "mvn")
    parser.add_argument("--local-repository", type=Path)
    parser.add_argument("--timeout-seconds", type=float, default=600,
                        help="Per-module bound; entries may override timeout_seconds")
    args = parser.parse_args()
    entries = json.loads(args.manifest.read_text(encoding="utf-8"))
    workspace = args.workspace.resolve()
    args.output.mkdir(parents=True, exist_ok=True)
    results = []
    java = str(Path(os.environ["JAVA_HOME"]) / "bin" / "java") if os.environ.get("JAVA_HOME") else "java"
    jdk = command_output([java, "-version"])
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
        timeout = float(entry.get("timeout_seconds", args.timeout_seconds))
        if not math.isfinite(timeout) or timeout <= 0:
            raise ValueError("timeout_seconds must be finite and positive")
        started_at = time.time_ns()
        start = time.monotonic()
        timed_out = False
        process = None
        try:
            if not pom.is_file():
                raise FileNotFoundError("Missing POM: " + str(pom))
            with log.open("w", encoding="utf-8") as output:
                process = subprocess.Popen(command, stdout=output, stderr=subprocess.STDOUT,
                                           start_new_session=os.name != "nt",
                                           creationflags=subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0)
                try:
                    process.wait(timeout=timeout)
                except subprocess.TimeoutExpired:
                    timed_out = True
                    stop_owned_process(process)
            code = 124 if timed_out else process.returncode
            error = "Timed out after " + str(timeout) + " seconds" if timed_out else None
        except OSError as failure:
            code, error = 127, str(failure)
        except BaseException:
            if process is not None and process.poll() is None:
                stop_owned_process(process)
            raise
        flaky_reports = []
        failed_reports = []
        seen_reports = set()
        for report in pom.parent.rglob("TEST-*.xml"):
            if report.parent.name not in ("surefire-reports", "failsafe-reports"):
                continue
            if report.stat().st_mtime_ns < started_at:
                continue
            report = report.resolve()
            if report in seen_reports:
                continue
            seen_reports.add(report)
            try:
                tree = ET.parse(report)
                if any(node.tag in ("flakyFailure", "flakyError") for node in tree.iter()):
                    flaky_reports.append(str(report))
                if any(node.tag in ("failure", "error") or
                       (node.tag == "testsuite" and
                        (int(node.get("failures", "0")) > 0 or int(node.get("errors", "0")) > 0))
                       for node in tree.iter()):
                    failed_reports.append(str(report))
            except (OSError, ET.ParseError, ValueError) as failure:
                code = code or 1
                error = "Cannot validate test report: " + str(failure)
        if flaky_reports:
            code = code or 1
            error = "Tests passed only after retries; inspect flaky_reports"
        if failed_reports:
            code = code or 1
            error = "Tests failed despite Maven's exit status; inspect failed_reports"
        results.append(dict(name=entry["name"], owner=entry["owner"], pom=str(pom),
                            command=command, exit=code, error=error, log=str(log),
                            flaky_reports=flaky_reports,
                            failed_reports=failed_reports, timed_out=timed_out,
                            timeout_seconds=timeout, jdk=jdk,
                            source_sha=command_output(["git", "rev-parse", "HEAD"], cwd=pom.parent),
                            seconds=round(time.monotonic() - start, 2)))
        (args.output / "results.json").write_text(json.dumps(results, indent=2), encoding="utf-8")
        print(entry["name"] + ": " + ("PASS" if code == 0 else "FAIL"), flush=True)
    return 1 if any(result["exit"] for result in results) else 0


if __name__ == "__main__":
    sys.exit(main())
