#!/bin/sh
set -eu
HERE=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
APP="$HERE/PZ-Server-Architect-0.3.0.jar"
JAVA=java
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then JAVA="$JAVA_HOME/bin/java"; fi
if [ -x "$HERE/runtime/bin/java" ]; then JAVA="$HERE/runtime/bin/java"; fi
if [ ! -f "$APP" ]; then printf '%s\n' 'Bitte zuerst das gesamte ZIP entpacken.' >&2; exit 1; fi
if ! "$JAVA" -jar "$APP" --version >/dev/null 2>&1; then
  printf '%s\n' 'PZ Server Architect benötigt Java 21 oder neuer.' 'Java fehlt oder ist über PATH/JAVA_HOME nicht erreichbar.' 'Hinweise: HANDBUCH.md. Die native Ausgabe enthält ihre Laufzeit.' >&2
  exit 1
fi
exec "$JAVA" -jar "$APP" "$@"
