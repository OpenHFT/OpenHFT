import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


@unittest.skipUnless(os.name == "posix", "This process-tree fixture requires POSIX signals")
class VerificationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="optional modules ")
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.runner = Path(os.environ.get("VERIFY_RUNNER", Path(__file__).with_name("verify-optional-modules.py")))
        self.maven = self.root / "fake maven"
        self.maven.write_text("#!" + sys.executable + "\n" + '''
import os, pathlib, signal, subprocess, sys, time
pom = pathlib.Path(sys.argv[sys.argv.index('-f') + 1])
mode = pom.parent.name
if mode == 'hang':
    child = subprocess.Popen([sys.executable, '-c',
        'import signal,time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(60)'])
    (pom.parent / 'child.pid').write_text(str(child.pid))
    time.sleep(60)
report = pom.parent / 'target' / 'surefire-reports' / 'TEST-fixture.xml'
report.parent.mkdir(parents=True, exist_ok=True)
node = '<' + mode + '/>' if mode in ('failure', 'error', 'flakyFailure') else ''
report.write_text('<testsuite><testcase>' + node + '</testcase></testsuite>')
''')
        self.maven.chmod(0o755)

    def run_entries(self, modes, extra=()):
        entries = []
        for mode in modes:
            directory = self.root / mode
            directory.mkdir(exist_ok=True)
            if mode != "missing":
                (directory / "pom.xml").write_text("<project/>")
            entries.append(dict(name=mode, owner="test", pom=mode + "/pom.xml"))
        manifest = self.root / "manifest.json"
        manifest.write_text(json.dumps(entries))
        result = subprocess.run([sys.executable, str(self.runner), str(manifest),
                                 "--workspace", str(self.root), "--output", str(self.root / "results"),
                                 "--maven", str(self.maven), *extra], capture_output=True, text=True, timeout=20)
        return result, json.loads((self.root / "results/results.json").read_text())

    def test_terminal_failed_reports_override_successful_maven(self):
        result, entries = self.run_entries(["failure", "error", "pass"])
        self.assertEqual(1, result.returncode)
        self.assertEqual([1, 1, 0], [entry["exit"] for entry in entries])
        self.assertTrue(entries[0]["failed_reports"])
        self.assertTrue(entries[1]["failed_reports"])

    def test_flaky_and_missing_modules_do_not_stop_following_modules(self):
        result, entries = self.run_entries(["flakyFailure", "missing", "pass"])
        self.assertEqual(1, result.returncode)
        self.assertEqual([1, 127, 0], [entry["exit"] for entry in entries])

    def test_timeout_stops_owned_child_and_continues(self):
        result, entries = self.run_entries(["hang", "pass"], ["--timeout-seconds", "1"])
        self.assertEqual(1, result.returncode)
        self.assertEqual([124, 0], [entry["exit"] for entry in entries])
        self.assertTrue(entries[0]["timed_out"])
        pid = int((self.root / "hang/child.pid").read_text())
        status = Path("/proc") / str(pid) / "status"
        if status.exists():
            # A child orphaned by its parent may await init's reaping, but cannot run.
            self.assertIn("\nState:\tZ", status.read_text())


if __name__ == "__main__":
    unittest.main()
