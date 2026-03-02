#!/usr/bin/env bash
#
# Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
#

set -u
set -o pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)

MODULE_DIR=""
QUALITY_DIR="$SCRIPT_DIR"
LOGS_DIR=""
ENFORCED="true"
JACOCO85="off"
PITEST80="off"
CPD_MIN_TOKENS="80"
SCOPE="full"
JAVADOC_DOCLINT_ALL="off"
MVN_CMD="${MVN:-mvn}"

declare -a RELEASE_ROOTS=()
declare -a RELEASE_ROOT_PREFIXES=()
declare -a JAVA_SOURCE_FILES=()
declare -a NON_RELEASE_JAVA_FILES=()
declare -A SOURCE_PATH_TO_FILE=()

PMD_PLUGIN_VERSION="3.26.0"
SPOTBUGS_PLUGIN_VERSION="4.8.6.6"
DEPENDENCY_PLUGIN_VERSION="3.6.1"
JACOCO_VERSION="0.8.12"
PITEST_PLUGIN_VERSION="1.17.2"

CHECKSTYLE_BASELINE_CFG="src/main/resources/net/openhft/quality/checkstyle26/chronicle-baseline-checkstyle.xml"
PMD_RULESET="src/main/resources/net/openhft/quality/pmd26/pmd-ruleset.xml"
SPOTBUGS_INCLUDE="src/main/resources/net/openhft/quality/spotbugs26/chronicle-spotbugs-include.xml"
SPOTBUGS_EXCLUDE="src/main/resources/net/openhft/quality/spotbugs26/chronicle-spotbugs-exclude.xml"

usage() {
  cat <<USAGE
Usage: $0 --module-dir <path> [options]

Applies quality checks to a Maven module without editing the module POM and
without installing this quality module into the target module first.

Required:
  --module-dir <path>         Path to target Maven module (must contain pom.xml)

Options:
  --quality-dir <path>        Path to this quality repo (default: script directory)
  --logs-dir <path>           Output directory for logs (default: <module>/logs/quality-profile)
  --enforced <true|false>     Fail on tool violations (default: true)
  --jacoco85 <on|off>         Enable optional 85%% line/branch gate (default: off)
  --pitest80 <on|off>         Enable optional PIT mutation gate at 80%% (default: off)
  --cpd-min-tokens <int>      PMD CPD minimum tokens (default: 80)
  --scope <full|non-release>  Scope checks to all files or only non-release files (default: full)
  --release-root <path>       Release source root to exclude when --scope non-release (repeatable)
                              Default release root in non-release scope: src/main/java
  --javadoc-doclint-all <on|off>
                              Enable javadoc gate with doclint=all across module sources (default: off)
  --mvn-cmd <command>         Maven command to use (default: mvn or MVN env)
  --help                      Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --module-dir)
      MODULE_DIR="$2"
      shift 2
      ;;
    --quality-dir)
      QUALITY_DIR="$2"
      shift 2
      ;;
    --logs-dir)
      LOGS_DIR="$2"
      shift 2
      ;;
    --enforced)
      ENFORCED="$2"
      shift 2
      ;;
    --jacoco85)
      JACOCO85="$2"
      shift 2
      ;;
    --pitest80)
      PITEST80="$2"
      shift 2
      ;;
    --cpd-min-tokens)
      CPD_MIN_TOKENS="$2"
      shift 2
      ;;
    --scope)
      SCOPE="$2"
      shift 2
      ;;
    --release-root)
      RELEASE_ROOTS+=("$2")
      shift 2
      ;;
    --javadoc-doclint-all)
      JAVADOC_DOCLINT_ALL="$2"
      shift 2
      ;;
    --mvn-cmd)
      MVN_CMD="$2"
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 2
      ;;
  esac
done

if [[ -z "$MODULE_DIR" ]]; then
  echo "--module-dir is required" >&2
  usage
  exit 2
fi

if [[ "$ENFORCED" != "true" && "$ENFORCED" != "false" ]]; then
  echo "--enforced must be true or false" >&2
  exit 2
fi

if [[ "$JACOCO85" != "on" && "$JACOCO85" != "off" ]]; then
  echo "--jacoco85 must be on or off" >&2
  exit 2
fi

if [[ "$PITEST80" != "on" && "$PITEST80" != "off" ]]; then
  echo "--pitest80 must be on or off" >&2
  exit 2
fi

if ! [[ "$CPD_MIN_TOKENS" =~ ^[0-9]+$ ]]; then
  echo "--cpd-min-tokens must be an integer" >&2
  exit 2
fi

if [[ "$SCOPE" != "full" && "$SCOPE" != "non-release" ]]; then
  echo "--scope must be full or non-release" >&2
  exit 2
fi

if [[ "$JAVADOC_DOCLINT_ALL" != "on" && "$JAVADOC_DOCLINT_ALL" != "off" ]]; then
  echo "--javadoc-doclint-all must be on or off" >&2
  exit 2
fi

MODULE_DIR=$(cd -- "$MODULE_DIR" && pwd)
QUALITY_DIR=$(cd -- "$QUALITY_DIR" && pwd)

if [[ ! -f "$MODULE_DIR/pom.xml" ]]; then
  echo "Target module pom.xml not found: $MODULE_DIR/pom.xml" >&2
  exit 2
fi

if [[ ! -f "$QUALITY_DIR/pom.xml" ]]; then
  echo "Quality pom.xml not found: $QUALITY_DIR/pom.xml" >&2
  exit 2
fi

if [[ "$SCOPE" == "non-release" && ${#RELEASE_ROOTS[@]} -eq 0 ]]; then
  RELEASE_ROOTS+=("src/main/java")
fi

normalize_root_path() {
  local root="$1"
  root="${root#./}"
  while [[ "$root" == */ ]]; do
    root="${root%/}"
  done
  if [[ -z "$root" ]]; then
    root="."
  fi
  printf "%s" "$root"
}

if [[ "$SCOPE" == "non-release" ]]; then
  for release_root in "${RELEASE_ROOTS[@]}"; do
    normalized_root=$(normalize_root_path "$release_root")
    if [[ "$normalized_root" == /* ]]; then
      RELEASE_ROOT_PREFIXES+=("${normalized_root%/}/")
    else
      RELEASE_ROOT_PREFIXES+=("$MODULE_DIR/${normalized_root%/}/")
    fi
  done
fi

if [[ -z "$LOGS_DIR" ]]; then
  LOGS_DIR="$MODULE_DIR/logs/quality-profile"
fi
mkdir -p "$LOGS_DIR"

REQUIRED_CMDS=("$MVN_CMD" java awk)
if [[ "$SCOPE" == "non-release" ]]; then
  REQUIRED_CMDS+=(xmlstarlet)
fi
for required_cmd in "${REQUIRED_CMDS[@]}"; do
  if ! command -v "$required_cmd" >/dev/null 2>&1; then
    echo "Required command not found: $required_cmd" >&2
    exit 2
  fi
done

required_file_or_exit() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Required file not found: $path" >&2
    exit 2
  fi
}

is_release_path() {
  local path="$1"
  local prefix

  if [[ "$path" != /* ]]; then
    path="$MODULE_DIR/$path"
  fi

  for prefix in "${RELEASE_ROOT_PREFIXES[@]}"; do
    if [[ "$path" == "${prefix%/}" || "$path" == "$prefix"* ]]; then
      return 0
    fi
  done

  return 1
}

resolve_source_path_to_file() {
  local source_path="$1"

  if [[ -n "${SOURCE_PATH_TO_FILE[$source_path]:-}" ]]; then
    printf "%s" "${SOURCE_PATH_TO_FILE[$source_path]}"
    return 0
  fi

  return 1
}

set_gate_error_status() {
  local step="$1"
  local rc="$2"

  if [[ "$ENFORCED" == "true" ]]; then
    STATUS["$step"]="FAIL"
    OVERALL_RC=1
  else
    STATUS["$step"]="WARN"
  fi

  echo "Step issue: $step (exit $rc). See $LOGS_DIR/${step}.log" >&2
}

set_gate_violation_status() {
  local step="$1"
  local finding_count="$2"
  local message="$3"

  if [[ "$finding_count" -eq 0 ]]; then
    STATUS["$step"]="PASS"
    return
  fi

  if [[ "$ENFORCED" == "true" ]]; then
    STATUS["$step"]="FAIL"
    OVERALL_RC=1
  else
    STATUS["$step"]="WARN"
  fi

  echo "$message" >&2
}

skip_gate() {
  local step="$1"
  local reason="$2"

  STATUS["$step"]="SKIP"
  printf "%s\n" "$reason" >"$LOGS_DIR/${step}.log"
}

emit_coverage_threshold_guidance() {
  local log="$1"

  {
    echo "coverage_guidance_1=Add coverage-focused scenarios in *CoverageTest.java, not golden-source behavior tests."
    echo "coverage_guidance_2=If a *CoverageTest.java fails unexpectedly, disable it with a reason (for example @Disabled(\"reason\")) and investigate; do not delete it."
  } >>"$log"

  echo "Coverage guidance: add coverage-focused scenarios in *CoverageTest.java, not golden-source behavior tests." >&2
  echo "Coverage guidance: if a *CoverageTest.java fails unexpectedly, disable it with a reason (for example @Disabled(\"reason\")) and investigate; do not delete it." >&2
}

required_file_or_exit "$QUALITY_DIR/$CHECKSTYLE_BASELINE_CFG"
required_file_or_exit "$QUALITY_DIR/$PMD_RULESET"
required_file_or_exit "$QUALITY_DIR/$SPOTBUGS_INCLUDE"
required_file_or_exit "$QUALITY_DIR/$SPOTBUGS_EXCLUDE"

for source_root in "$MODULE_DIR/src/main/java" "$MODULE_DIR/src/test/java"; do
  if [[ -d "$source_root" ]]; then
    while IFS= read -r -d '' java_file; do
      JAVA_SOURCE_FILES+=("$java_file")

      if [[ "$java_file" == "$MODULE_DIR/src/main/java/"* ]]; then
        source_rel="${java_file#"$MODULE_DIR/src/main/java/"}"
        SOURCE_PATH_TO_FILE["$source_rel"]="$java_file"
      elif [[ "$java_file" == "$MODULE_DIR/src/test/java/"* ]]; then
        source_rel="${java_file#"$MODULE_DIR/src/test/java/"}"
        SOURCE_PATH_TO_FILE["$source_rel"]="$java_file"
      fi
    done < <(find "$source_root" -type f -name '*.java' -print0)
  fi
done

if [[ ${#JAVA_SOURCE_FILES[@]} -eq 0 ]]; then
  echo "No src/main/java or src/test/java found under $MODULE_DIR" >&2
  exit 2
fi

if [[ "$SCOPE" == "non-release" ]]; then
  for java_file in "${JAVA_SOURCE_FILES[@]}"; do
    if ! is_release_path "$java_file"; then
      NON_RELEASE_JAVA_FILES+=("$java_file")
    fi
  done
fi

SCOPE_LOG="$LOGS_DIR/scope_filter.log"
{
  echo "scope=$SCOPE"
  if [[ "$SCOPE" == "non-release" ]]; then
    echo "release_roots=${RELEASE_ROOTS[*]}"
    echo "total_java_files=${#JAVA_SOURCE_FILES[@]}"
    echo "non_release_java_files=${#NON_RELEASE_JAVA_FILES[@]}"
  fi
} >"$SCOPE_LOG"

declare -A STATUS

run_required() {
  local name="$1"
  shift
  local log="$LOGS_DIR/${name}.log"
  echo "==> [$name]"
  if "$@" >"$log" 2>&1; then
    STATUS["$name"]="PASS"
  else
    local rc=$?
    STATUS["$name"]="FAIL"
    echo "Step failed: $name (exit $rc). See $log" >&2
    exit "$rc"
  fi
}

OVERALL_RC=0

run_gate() {
  local name="$1"
  shift
  local log="$LOGS_DIR/${name}.log"
  echo "==> [$name]"
  if "$@" >"$log" 2>&1; then
    STATUS["$name"]="PASS"
  else
    local rc=$?
    if [[ "$ENFORCED" == "true" ]]; then
      STATUS["$name"]="FAIL"
      OVERALL_RC=1
    else
      STATUS["$name"]="WARN"
    fi
    echo "Step issue: $name (exit $rc). See $log" >&2
  fi
}

run_required bootstrap_quality_classes \
  "$MVN_CMD" -q -f "$QUALITY_DIR/pom.xml" \
  -DskipTests -Dcheckstyle.skip=true -Dspotbugs.skip=true -Dpmd.skip=true -Dmaven.javadoc.skip=true \
  compile

CHECKSTYLE_CP_FILE="$LOGS_DIR/checkstyle.classpath"
run_required resolve_checkstyle_classpath \
  "$MVN_CMD" -q -f "$QUALITY_DIR/pom.xml" \
  -DincludeScope=provided \
  -Dmdep.outputFile="$CHECKSTYLE_CP_FILE" \
  "org.apache.maven.plugins:maven-dependency-plugin:${DEPENDENCY_PLUGIN_VERSION}:build-classpath"

if [[ ! -s "$CHECKSTYLE_CP_FILE" ]]; then
  echo "Checkstyle classpath file is empty: $CHECKSTYLE_CP_FILE" >&2
  exit 2
fi

CHECKSTYLE_CP="$(cat "$CHECKSTYLE_CP_FILE"):$QUALITY_DIR/target/classes:$QUALITY_DIR/src/main/resources"

CHECKSTYLE_INPUTS=()
if [[ "$SCOPE" == "non-release" ]]; then
  CHECKSTYLE_INPUTS=("${NON_RELEASE_JAVA_FILES[@]}")
else
  if [[ -d "$MODULE_DIR/src/main/java" ]]; then
    CHECKSTYLE_INPUTS+=("$MODULE_DIR/src/main/java")
  fi
  if [[ -d "$MODULE_DIR/src/test/java" ]]; then
    CHECKSTYLE_INPUTS+=("$MODULE_DIR/src/test/java")
  fi
fi

if [[ ${#CHECKSTYLE_INPUTS[@]} -eq 0 ]]; then
  if [[ "$SCOPE" == "non-release" ]]; then
    skip_gate checkstyle_baseline "No non-release Java files to check."
  else
    echo "No src/main/java or src/test/java found under $MODULE_DIR" >&2
    exit 2
  fi
else
  run_gate checkstyle_baseline \
    java -cp "$CHECKSTYLE_CP" com.puppycrawl.tools.checkstyle.Main \
    -c "$QUALITY_DIR/$CHECKSTYLE_BASELINE_CFG" \
    "${CHECKSTYLE_INPUTS[@]}"
fi

if [[ "$SCOPE" == "non-release" ]]; then
  if [[ ${#NON_RELEASE_JAVA_FILES[@]} -eq 0 ]]; then
    skip_gate pmd_check "No non-release Java files to check."
  else
    PMD_LOG="$LOGS_DIR/pmd_check.log"
    PMD_XML="$MODULE_DIR/target/pmd.xml"
    PMD_FINDINGS_FILE="$LOGS_DIR/pmd.non_release.findings.txt"

    echo "==> [pmd_check]"
    if "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
      -DskipTests \
      -Dpmd.includeTests=true \
      -Dpmd.rulesets="$QUALITY_DIR/$PMD_RULESET" \
      -Dpmd.failOnViolation=false \
      "org.apache.maven.plugins:maven-pmd-plugin:${PMD_PLUGIN_VERSION}:pmd" >"$PMD_LOG" 2>&1; then

      if [[ ! -f "$PMD_XML" ]]; then
        echo "PMD XML report not found: $PMD_XML" >>"$PMD_LOG"
        set_gate_error_status pmd_check 2
      else
        pmd_parse_failed="false"
        if ! xmlstarlet sel -t \
          -m "/*[local-name()='pmd']/*[local-name()='file']/*[local-name()='violation']" \
          -v "concat(../@name,'|',@beginline,'|',@rule)" -n \
          "$PMD_XML" >"$PMD_FINDINGS_FILE" 2>>"$PMD_LOG"; then
          rc=$?
          if [[ "$rc" -gt 1 ]]; then
            echo "Failed to parse PMD XML report: $PMD_XML" >>"$PMD_LOG"
            set_gate_error_status pmd_check "$rc"
            pmd_parse_failed="true"
          fi
          : >"$PMD_FINDINGS_FILE"
        fi

        if [[ "$pmd_parse_failed" == "false" ]]; then
          PMD_NON_RELEASE_COUNT=0
          while IFS='|' read -r file_path begin_line rule_name; do
            [[ -z "$file_path" ]] && continue
            if ! is_release_path "$file_path"; then
              PMD_NON_RELEASE_COUNT=$((PMD_NON_RELEASE_COUNT + 1))
              printf "%s:%s:%s\n" "$file_path" "$begin_line" "$rule_name" >>"$PMD_LOG"
            fi
          done <"$PMD_FINDINGS_FILE"

          if [[ "$PMD_NON_RELEASE_COUNT" -eq 0 ]]; then
            STATUS[pmd_check]="PASS"
            echo "No non-release PMD violations." >>"$PMD_LOG"
          else
            set_gate_violation_status pmd_check "$PMD_NON_RELEASE_COUNT" \
              "PMD reported $PMD_NON_RELEASE_COUNT non-release violation(s). See $PMD_LOG"
          fi
        fi
      fi
    else
      rc=$?
      set_gate_error_status pmd_check "$rc"
    fi
  fi
else
  run_gate pmd_check \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -DskipTests \
    -Dpmd.includeTests=true \
    -Dpmd.failOnViolation="$ENFORCED" \
    -Dpmd.rulesets="$QUALITY_DIR/$PMD_RULESET" \
    "org.apache.maven.plugins:maven-pmd-plugin:${PMD_PLUGIN_VERSION}:check"
fi

if [[ "$SCOPE" == "non-release" ]]; then
  if [[ ${#NON_RELEASE_JAVA_FILES[@]} -eq 0 ]]; then
    skip_gate pmd_cpd_check "No non-release Java files to check."
  else
    CPD_LOG="$LOGS_DIR/pmd_cpd_check.log"
    CPD_XML="$MODULE_DIR/target/cpd.xml"
    CPD_FINDINGS_FILE="$LOGS_DIR/cpd.non_release.findings.txt"

    echo "==> [pmd_cpd_check]"
    if "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
      -DskipTests \
      -DminimumTokens="$CPD_MIN_TOKENS" \
      -Dcpd.failOnViolation=false \
      "org.apache.maven.plugins:maven-pmd-plugin:${PMD_PLUGIN_VERSION}:cpd" >"$CPD_LOG" 2>&1; then

      if [[ ! -f "$CPD_XML" ]]; then
        echo "CPD XML report not found: $CPD_XML" >>"$CPD_LOG"
        set_gate_error_status pmd_cpd_check 2
      else
        cpd_parse_failed="false"
        if ! xmlstarlet sel -t \
          -m "/*[local-name()='pmd-cpd']/*[local-name()='duplication']/*[local-name()='file']" \
          -v "concat(count(../preceding-sibling::*[local-name()='duplication']) + 1,'|',@path,'|',@line)" -n \
          "$CPD_XML" >"$CPD_FINDINGS_FILE" 2>>"$CPD_LOG"; then
          rc=$?
          if [[ "$rc" -gt 1 ]]; then
            echo "Failed to parse CPD XML report: $CPD_XML" >>"$CPD_LOG"
            set_gate_error_status pmd_cpd_check "$rc"
            cpd_parse_failed="true"
          fi
          : >"$CPD_FINDINGS_FILE"
        fi

        if [[ "$cpd_parse_failed" == "false" ]]; then
          declare -A CPD_NON_RELEASE_DUPS=()

          while IFS='|' read -r duplication_id file_path line_number; do
            [[ -z "$duplication_id" || -z "$file_path" ]] && continue

            if ! is_release_path "$file_path"; then
              CPD_NON_RELEASE_DUPS["$duplication_id"]=1
              printf "duplication=%s file=%s line=%s\n" "$duplication_id" "$file_path" "$line_number" >>"$CPD_LOG"
            fi
          done <"$CPD_FINDINGS_FILE"

          CPD_NON_RELEASE_COUNT=${#CPD_NON_RELEASE_DUPS[@]}
          if [[ "$CPD_NON_RELEASE_COUNT" -eq 0 ]]; then
            STATUS[pmd_cpd_check]="PASS"
            echo "No non-release CPD duplications." >>"$CPD_LOG"
          else
            set_gate_violation_status pmd_cpd_check "$CPD_NON_RELEASE_COUNT" \
              "CPD reported $CPD_NON_RELEASE_COUNT non-release duplication group(s). See $CPD_LOG"
          fi
        fi
      fi
    else
      rc=$?
      set_gate_error_status pmd_cpd_check "$rc"
    fi
  fi
else
  run_gate pmd_cpd_check \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -DskipTests \
    -Dpmd.failOnViolation="$ENFORCED" \
    -DfailOnViolation="$ENFORCED" \
    -DminimumTokens="$CPD_MIN_TOKENS" \
    "org.apache.maven.plugins:maven-pmd-plugin:${PMD_PLUGIN_VERSION}:cpd-check"
fi

run_required module_test_compile \
  "$MVN_CMD" -f "$MODULE_DIR/pom.xml" -DskipTests test-compile

if [[ "$SCOPE" == "non-release" ]]; then
  if [[ ${#NON_RELEASE_JAVA_FILES[@]} -eq 0 ]]; then
    skip_gate spotbugs_check "No non-release Java files to check."
  else
    SPOTBUGS_LOG="$LOGS_DIR/spotbugs_check.log"
    SPOTBUGS_XML="$MODULE_DIR/target/spotbugsXml.xml"
    SPOTBUGS_FINDINGS_FILE="$LOGS_DIR/spotbugs.non_release.findings.txt"

    echo "==> [spotbugs_check]"
    if "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
      -Dspotbugs.includeTests=true \
      -Dspotbugs.effort=Max \
      -Dspotbugs.threshold=Low \
      -Dspotbugs.failOnError=false \
      -Dspotbugs.xmlOutput=true \
      -Dspotbugs.includeFilterFile="$QUALITY_DIR/$SPOTBUGS_INCLUDE" \
      -Dspotbugs.excludeFilterFile="$QUALITY_DIR/$SPOTBUGS_EXCLUDE" \
      "com.github.spotbugs:spotbugs-maven-plugin:${SPOTBUGS_PLUGIN_VERSION}:spotbugs" >"$SPOTBUGS_LOG" 2>&1; then

      if [[ ! -f "$SPOTBUGS_XML" ]]; then
        echo "SpotBugs XML report not found: $SPOTBUGS_XML" >>"$SPOTBUGS_LOG"
        set_gate_error_status spotbugs_check 2
      else
        spotbugs_parse_failed="false"
        if ! xmlstarlet sel -t \
          -m "//*[local-name()='FileStats'][number(@bugCount) > 0]" \
          -v "concat(@path,'|',@bugCount)" -n \
          "$SPOTBUGS_XML" >"$SPOTBUGS_FINDINGS_FILE" 2>>"$SPOTBUGS_LOG"; then
          rc=$?
          if [[ "$rc" -gt 1 ]]; then
            echo "Failed to parse SpotBugs XML report: $SPOTBUGS_XML" >>"$SPOTBUGS_LOG"
            set_gate_error_status spotbugs_check "$rc"
            spotbugs_parse_failed="true"
          fi
          : >"$SPOTBUGS_FINDINGS_FILE"
        fi

        if [[ "$spotbugs_parse_failed" == "false" ]]; then
          SPOTBUGS_NON_RELEASE_COUNT=0

          while IFS='|' read -r source_path bug_count; do
            [[ -z "$source_path" ]] && continue
            if ! [[ "$bug_count" =~ ^[0-9]+$ ]]; then
              continue
            fi

            display_path="$source_path"
            if resolved_path=$(resolve_source_path_to_file "$source_path"); then
              display_path="$resolved_path"
              if is_release_path "$resolved_path"; then
                continue
              fi
            fi

            SPOTBUGS_NON_RELEASE_COUNT=$((SPOTBUGS_NON_RELEASE_COUNT + bug_count))
            printf "%s|%s\n" "$display_path" "$bug_count" >>"$SPOTBUGS_LOG"
          done <"$SPOTBUGS_FINDINGS_FILE"

          if [[ "$SPOTBUGS_NON_RELEASE_COUNT" -eq 0 ]]; then
            STATUS[spotbugs_check]="PASS"
            echo "No non-release SpotBugs findings." >>"$SPOTBUGS_LOG"
          else
            set_gate_violation_status spotbugs_check "$SPOTBUGS_NON_RELEASE_COUNT" \
              "SpotBugs reported $SPOTBUGS_NON_RELEASE_COUNT non-release finding(s). See $SPOTBUGS_LOG"
          fi
        fi
      fi
    else
      rc=$?
      set_gate_error_status spotbugs_check "$rc"
    fi
  fi
else
  run_gate spotbugs_check \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -Dspotbugs.includeTests=true \
    -Dspotbugs.effort=Max \
    -Dspotbugs.threshold=Low \
    -Dspotbugs.failOnError="$ENFORCED" \
    -Dspotbugs.includeFilterFile="$QUALITY_DIR/$SPOTBUGS_INCLUDE" \
    -Dspotbugs.excludeFilterFile="$QUALITY_DIR/$SPOTBUGS_EXCLUDE" \
    "com.github.spotbugs:spotbugs-maven-plugin:${SPOTBUGS_PLUGIN_VERSION}:check"
fi

if [[ "$JAVADOC_DOCLINT_ALL" == "on" ]]; then
  run_gate javadoc_doclint_all \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -DskipTests \
    -Dmaven.javadoc.skip=false \
    -Ddoclint=all \
    -Dmaven.javadoc.failOnWarnings=true \
    "org.apache.maven.plugins:maven-javadoc-plugin:javadoc"
else
  STATUS[javadoc_doclint_all]="SKIP"
fi

if [[ "$JACOCO85" == "on" ]]; then
  JACOCO_AGENT_JAR="$HOME/.m2/repository/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
  run_required jacoco_fetch_agent \
    "$MVN_CMD" -q -f "$QUALITY_DIR/pom.xml" \
    "org.apache.maven.plugins:maven-dependency-plugin:${DEPENDENCY_PLUGIN_VERSION}:get" \
    -Dartifact="org.jacoco:org.jacoco.agent:${JACOCO_VERSION}:jar:runtime"

  required_file_or_exit "$JACOCO_AGENT_JAR"

  JACOCO_EXEC="$MODULE_DIR/target/jacoco-quality-profile.exec"

  run_required jacoco_test_with_agent \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -DargLine="-javaagent:${JACOCO_AGENT_JAR}=destfile=${JACOCO_EXEC}" \
    test

  run_required jacoco_report \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -Djacoco.dataFile="$JACOCO_EXEC" \
    "org.jacoco:jacoco-maven-plugin:${JACOCO_VERSION}:report"

  JACOCO_CSV="$MODULE_DIR/target/site/jacoco/jacoco.csv"
  if [[ ! -f "$JACOCO_CSV" ]]; then
    STATUS[jacoco_85_gate]="FAIL"
    OVERALL_RC=1
    echo "JaCoCo CSV report not found: $JACOCO_CSV" >&2
    {
      echo "error=JaCoCo CSV report not found"
      echo "expected_csv=$JACOCO_CSV"
    } >"$LOGS_DIR/jacoco_85_gate.log"
    emit_coverage_threshold_guidance "$LOGS_DIR/jacoco_85_gate.log"
  else
    ratios=$(awk -F, 'NR>1 {lm+=$8; lc+=$9; bm+=$6; bc+=$7} END {\
      line=(lc+lm>0)?(lc/(lc+lm)):1.0; \
      branch=(bc+bm>0)?(bc/(bc+bm)):1.0; \
      printf "%.6f %.6f", line, branch }' "$JACOCO_CSV")

    line_ratio=$(echo "$ratios" | awk '{print $1}')
    branch_ratio=$(echo "$ratios" | awk '{print $2}')

    line_ok=$(awk -v r="$line_ratio" 'BEGIN{print (r>=0.85)?1:0}')
    branch_ok=$(awk -v r="$branch_ratio" 'BEGIN{print (r>=0.85)?1:0}')

    {
      echo "line_ratio=$line_ratio"
      echo "branch_ratio=$branch_ratio"
      echo "line_threshold=0.85"
      echo "branch_threshold=0.85"
    } >"$LOGS_DIR/jacoco_85_gate.log"

    if [[ "$line_ok" == "1" && "$branch_ok" == "1" ]]; then
      STATUS[jacoco_85_gate]="PASS"
    else
      if [[ "$ENFORCED" == "true" ]]; then
        STATUS[jacoco_85_gate]="FAIL"
        OVERALL_RC=1
      else
        STATUS[jacoco_85_gate]="WARN"
      fi
      echo "JaCoCo 85%% gate not met (line=$line_ratio branch=$branch_ratio)." >&2
      emit_coverage_threshold_guidance "$LOGS_DIR/jacoco_85_gate.log"
    fi
  fi
else
  STATUS[jacoco_85_gate]="SKIP"
fi

if [[ "$PITEST80" == "on" ]]; then
  run_gate pitest_80_gate \
    "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
    -DskipTests=false \
    -DmutationThreshold=80 \
    -DoutputFormats=XML,HTML \
    "org.pitest:pitest-maven:${PITEST_PLUGIN_VERSION}:mutationCoverage"
else
  STATUS[pitest_80_gate]="SKIP"
fi

echo
echo "Quality profile summary"
echo "  module:   $MODULE_DIR"
echo "  quality:  $QUALITY_DIR"
echo "  enforced: $ENFORCED"
echo "  scope:    $SCOPE"
if [[ "$SCOPE" == "non-release" ]]; then
  echo "  release:  ${RELEASE_ROOTS[*]}"
  echo "  scoped:   ${#NON_RELEASE_JAVA_FILES[@]} non-release Java files"
fi
echo "  jacoco85: $JACOCO85"
echo "  pitest80: $PITEST80"
echo "  javadoc:  $JAVADOC_DOCLINT_ALL"
echo "  logs:     $LOGS_DIR"
echo
printf "%-24s %-6s %s\n" "STEP" "STATE" "LOG"
for step in \
  bootstrap_quality_classes \
  resolve_checkstyle_classpath \
  checkstyle_baseline \
  pmd_check \
  pmd_cpd_check \
  module_test_compile \
  spotbugs_check \
  javadoc_doclint_all \
  jacoco_fetch_agent \
  jacoco_test_with_agent \
  jacoco_report \
  jacoco_85_gate \
  pitest_80_gate; do
  if [[ -n "${STATUS[$step]:-}" ]]; then
    printf "%-24s %-6s %s\n" "$step" "${STATUS[$step]}" "$LOGS_DIR/${step}.log"
  fi
done

if [[ "$OVERALL_RC" -ne 0 ]]; then
  exit "$OVERALL_RC"
fi

exit 0
