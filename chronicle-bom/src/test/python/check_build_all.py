"""Check imported BOM profile activation with real Maven consumer models."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import tempfile
import uuid
import xml.etree.ElementTree as ET


NS = {'m': 'http://maven.apache.org/POM/4.0.0'}
BOM = Path(__file__).resolve().parents[3] / 'pom.xml'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--maven', default='mvn.cmd' if os.name == 'nt' else 'mvn')
    parser.add_argument('--repository', type=Path, required=True)
    args = parser.parse_args()
    source = ET.parse(BOM).getroot()
    version = source.findtext('m:version', namespaces=NS)
    staged_version = '0-build-all-check-' + uuid.uuid4().hex + '-SNAPSHOT'
    work_root = BOM.parent / 'target' / 'consumer-checks'
    work_root.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(prefix='consumer-', dir=work_root) as temporary:
        work = Path(temporary).resolve()
        assert work.is_relative_to(work_root.resolve())
        staged = work / 'bom.xml'
        original = BOM.read_text(encoding='utf-8')
        staged.write_text(original.replace('<version>' + version + '</version>',
                                          '<version>' + staged_version + '</version>', 1), encoding='utf-8')
        consumer = work / 'pom.xml'
        consumer.write_text('''<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>test</groupId><artifactId>bom-consumer</artifactId><version>1</version>
  <packaging>pom</packaging>
  <dependencyManagement><dependencies><dependency>
    <groupId>net.openhft</groupId><artifactId>chronicle-bom</artifactId>
    <version>''' + staged_version + '''</version><type>pom</type><scope>import</scope>
  </dependency></dependencies></dependencyManagement>
</project>
''', encoding='utf-8')

        def maven(label, arguments, pom=consumer):
            command = [args.maven, '-B', '-ntp', '-nsu', '-f', str(pom),
                       '-Dmaven.repo.local=' + str(args.repository.resolve())] + arguments
            result = subprocess.run(command, cwd=work, capture_output=True, text=True, timeout=180)
            if result.returncode:
                raise AssertionError(label + '\n' + result.stdout + result.stderr)

        # A unique local coordinate avoids overwriting the development BOM.
        maven('stage BOM', ['org.apache.maven.plugins:maven-install-plugin:3.1.1:install-file',
                           '-Dfile=' + str(staged), '-DpomFile=' + str(staged)], pom=staged)

        results = {}
        for label, options in [('default', []), ('false', ['-Dbuild-all=false']),
                               ('true', ['-Dbuild-all=true']),
                               ('override', ['-Dbuild-all=true', '-Dchronicle.queue.version=999-local-control'])]:
            effective = work / (label + '.xml')
            maven(label, ['org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom',
                          '-Doutput=' + str(effective)] + options)
            dependencies = ET.parse(effective).findall('m:dependencyManagement/m:dependencies/m:dependency', NS)
            managed = {(d.findtext('m:artifactId', namespaces=NS), d.findtext('m:type', 'jar', NS)):
                       d.findtext('m:version', namespaces=NS) for d in dependencies}
            # Build All does not build Core's standalone assertion modules.
            for artifact in ('assertions-enabled', 'assertions-disabled'):
                assert managed[(artifact, 'jar')] == '1.0.0', (label, artifact, managed[(artifact, 'jar')])
            queue = managed[('chronicle-queue', 'jar')]
            assert queue == managed[('chronicle-queue', 'test-jar')], (label, 'Queue main/test mismatch')
            if label == 'override':
                assert queue == '999-local-control', queue
            else:
                for artifact in ['chronicle-core', 'chronicle-bytes', 'chronicle-threads',
                                 'chronicle-wire', 'chronicle-queue', 'chronicle-queue-enterprise']:
                    selected = managed[(artifact, 'jar')]
                    assert selected.endswith('-SNAPSHOT') == (label == 'true'), (label, artifact, selected)
            results[label] = queue
            print(label + ' PASS: Queue main/test ' + queue, flush=True)
        assert results['default'] == results['false']
        (work_root / 'results.json').write_text(json.dumps(results, indent=2), encoding='utf-8')


if __name__ == '__main__':
    main()
