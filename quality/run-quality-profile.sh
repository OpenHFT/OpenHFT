#!/usr/bin/env bash

set -u
set -o pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)

MODULE_DIR=""
QUALITY_DIR="$SCRIPT_DIR"
LOGS_DIR=""
ENFORCED="true"
JACOCO85="off"
PITEST80="off"
CPD_MIN_TOKENS="100"
MVN_CMD="${MVN:-mvn}"

PMD_PLUGIN_VERSION="3.26.0"
SPOTBUGS_PLUGIN_VERSION="4.8.6.6"
DEPENDENCY_PLUGIN_VERSION="3.6.1"
JACOCO_VERSION="0.8.12"
PITEST_PLUGIN_VERSION="1.22.0"

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
  --cpd-min-tokens <int>      PMD CPD minimum tokens (default: 100)
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

if [[ -z "$LOGS_DIR" ]]; then
  LOGS_DIR="$MODULE_DIR/logs/quality-profile"
fi
mkdir -p "$LOGS_DIR"

for required_cmd in "$MVN_CMD" java awk; do
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

required_file_or_exit "$QUALITY_DIR/$CHECKSTYLE_BASELINE_CFG"
required_file_or_exit "$QUALITY_DIR/$PMD_RULESET"
required_file_or_exit "$QUALITY_DIR/$SPOTBUGS_INCLUDE"
required_file_or_exit "$QUALITY_DIR/$SPOTBUGS_EXCLUDE"

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
if [[ -d "$MODULE_DIR/src/main/java" ]]; then
  CHECKSTYLE_INPUTS+=("$MODULE_DIR/src/main/java")
fi
if [[ -d "$MODULE_DIR/src/test/java" ]]; then
  CHECKSTYLE_INPUTS+=("$MODULE_DIR/src/test/java")
fi
if [[ ${#CHECKSTYLE_INPUTS[@]} -eq 0 ]]; then
  echo "No src/main/java or src/test/java found under $MODULE_DIR" >&2
  exit 2
fi

run_gate checkstyle_baseline \
  java -cp "$CHECKSTYLE_CP" com.puppycrawl.tools.checkstyle.Main \
  -c "$QUALITY_DIR/$CHECKSTYLE_BASELINE_CFG" \
  "${CHECKSTYLE_INPUTS[@]}"

run_gate pmd_check \
  "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
  -DskipTests \
  -Dpmd.includeTests=true \
  -Dpmd.failOnViolation="$ENFORCED" \
  -Dpmd.rulesets="$QUALITY_DIR/$PMD_RULESET" \
  "org.apache.maven.plugins:maven-pmd-plugin:${PMD_PLUGIN_VERSION}:check"

run_gate pmd_cpd_check \
  "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
  -DskipTests \
  -Dpmd.failOnViolation="$ENFORCED" \
  -DfailOnViolation="$ENFORCED" \
  -DminimumTokens="$CPD_MIN_TOKENS" \
  "org.apache.maven.plugins:maven-pmd-plugin:${PMD_PLUGIN_VERSION}:cpd-check"

run_required module_test_compile \
  "$MVN_CMD" -f "$MODULE_DIR/pom.xml" -DskipTests test-compile

run_gate spotbugs_check \
  "$MVN_CMD" -f "$MODULE_DIR/pom.xml" \
  -Dspotbugs.includeTests=true \
  -Dspotbugs.effort=Max \
  -Dspotbugs.threshold=Low \
  -Dspotbugs.failOnError="$ENFORCED" \
  -Dspotbugs.includeFilterFile="$QUALITY_DIR/$SPOTBUGS_INCLUDE" \
  -Dspotbugs.excludeFilterFile="$QUALITY_DIR/$SPOTBUGS_EXCLUDE" \
  "com.github.spotbugs:spotbugs-maven-plugin:${SPOTBUGS_PLUGIN_VERSION}:check"

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
  else
    ratios=$(awk -F, 'NR>1 {lm+=$8; lc+=$9; bm+=$6; bc+=$7} END {\
      line=(lc+lm>0)?(lc/(lc+lm)):1.0; \
      branch=(bc+bm>0)?(bc/(bc+bm)):1.0; \
      printf "%.6f %.6f", line, branch }' "$JACOCO_CSV")

    line_ratio=$(echo "$ratios" | awk '{print $1}')
    branch_ratio=$(echo "$ratios" | awk '{print $2}')

    line_ok=$(awk -v r="$line_ratio" 'BEGIN{print (r>=0.90)?1:0}')
    branch_ok=$(awk -v r="$branch_ratio" 'BEGIN{print (r>=0.85)?1:0}')

    {
      echo "line_ratio=$line_ratio"
      echo "branch_ratio=$branch_ratio"
      echo "line_threshold=0.90"
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
      echo "JaCoCo 90/85 gate not met (line=$line_ratio branch=$branch_ratio)." >&2
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
echo "  jacoco85: $JACOCO85"
echo "  pitest80: $PITEST80"
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
