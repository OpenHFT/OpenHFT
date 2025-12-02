#!/usr/bin/env bash
# Summarise non-OpenHFT/Chronicle dependencies across sibling Maven projects.
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"
parent_dir="$(cd "${repo_root}/.." && pwd)"

target_root="${1:-${parent_dir}}"

python3 - <<'PY' "${target_root}"
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1]).resolve()
excluded_prefixes = ("net.openhft", "chronicle")

def pom_paths(base: Path):
    for path in base.rglob("pom.xml"):
        # Skip typical build/target directories
        if any(part in ("target", "build", ".git") for part in path.parts):
            continue
        yield path

def collect_deps(pom: Path):
    try:
        tree = ET.parse(pom)
    except ET.ParseError:
        return []
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    deps = []
    for section in ("dependencies", "dependencyManagement"):
        for dep in tree.findall(f".//m:{section}/m:dependencies/m:dependency", ns):
            gid = dep.findtext("m:groupId", default=None, namespaces=ns)
            aid = dep.findtext("m:artifactId", default=None, namespaces=ns)
            ver = dep.findtext("m:version", default="", namespaces=ns)
            scope = dep.findtext("m:scope", default="", namespaces=ns)
            if not gid or not aid:
                continue
            if gid.startswith(excluded_prefixes):
                continue
            deps.append((gid.strip(), aid.strip(), ver.strip(), scope.strip() or "compile"))
    return deps

summary = {}

for pom in pom_paths(root):
    deps = collect_deps(pom)
    if not deps:
        continue
    for gid, aid, ver, scope in deps:
        key = (gid, aid)
        entry = summary.setdefault(key, {"versions": set(), "scopes": set(), "locations": []})
        if ver:
            entry["versions"].add(ver)
        if scope:
            entry["scopes"].add(scope)
        entry["locations"].append(str(pom.parent))

def format_versions(vers):
    if not vers:
        return "n/a"
    if len(vers) == 1:
        return next(iter(vers))
    return ", ".join(sorted(vers))

for (gid, aid), data in sorted(summary.items()):
    versions = format_versions(data["versions"])
    scopes = ", ".join(sorted(data["scopes"])) if data["scopes"] else "n/a"
    print(f"{gid}:{aid} :: versions [{versions}] :: scopes [{scopes}] :: used in {len(data['locations'])} module(s)")
PY
