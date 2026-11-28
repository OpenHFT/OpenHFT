"""Exercise the Unix launcher with a real checker: python3 verify.py JDK_HOME."""
import os
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path


def verify(jdk):
    launcher = Path(__file__).resolve().parent
    env = os.environ.copy()
    env['_JAVA_OPTIONS'] = '-Xms64m -Xmx512m'
    env['JAVA_HOME'] = str(jdk)
    env['PATH'] = os.pathsep.join((str(launcher), str(launcher), str(jdk / 'bin'), env['PATH']))
    with tempfile.TemporaryDirectory(prefix='chronicle checker ') as directory:
        work = Path(directory)
        jars = []
        for name, method in [('old', 'public int value() { return 1; }'), ('removed', '')]:
            classes = work / name
            classes.mkdir()
            source = classes / 'Example.java'
            source.write_text('public class Example { ' + method + ' }', encoding='ascii')
            subprocess.run([str(jdk / 'bin/javac'), '-source', '8', '-target', '8', str(source)],
                           env=env, check=True, timeout=30)
            jar = classes / 'example.jar'
            with zipfile.ZipFile(jar, 'w') as archive:
                archive.write(classes / 'Example.class', 'Example.class')
            jars.append(jar)
        for label, new_jar, expected in [('unchanged', jars[0], 0), ('removed', jars[1], 1)]:
            report = work / (label + '.html')
            result = subprocess.run([str(launcher / 'japi-compliance-checker'),
                '-jdk-path', str(jdk), '-lib', 'example', '-v1', '1', '-v2', '2',
                str(jars[0]), str(new_jar), '-report-path', str(report)],
                env=env, capture_output=True, text=True, timeout=30)
            assert result.returncode == expected, result.stdout + result.stderr
            assert report.is_file() and report.stat().st_size > 0, result.stdout
            assert 'Binary compatibility: ' in result.stdout, result.stdout
            print(label + ': expected exit ' + str(expected) + ', report produced', flush=True)
        env['PATH'] = str(launcher) + os.pathsep + str(launcher)
        result = subprocess.run([str(launcher / 'japi-compliance-checker'), '--version'],
                                env=env, capture_output=True, text=True, timeout=10)
        assert result.returncode == 127, result.stdout + result.stderr
        assert 'No installed' in result.stderr, result.stderr
        print('missing checker: exit 127, no recursion', flush=True)


if __name__ == '__main__':
    verify(Path(sys.argv[1]).resolve())
