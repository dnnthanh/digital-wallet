#!/bin/sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_VERSION=3.9.16
MAVEN_HOME="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/apache-maven-$MAVEN_VERSION"
MVN="$MAVEN_HOME/bin/mvn"
if [ ! -x "$MVN" ]; then
  ZIP="${TMPDIR:-/tmp}/apache-maven-$MAVEN_VERSION-bin.zip"
  URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
  mkdir -p "$(dirname "$MAVEN_HOME")"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$URL" -o "$ZIP"; else wget -q "$URL" -O "$ZIP"; fi
  rm -rf "$MAVEN_HOME"
  mkdir -p "$MAVEN_HOME"
  python3 - "$ZIP" "$MAVEN_HOME" <<'PY'
import sys, zipfile, pathlib
z=zipfile.ZipFile(sys.argv[1]); out=pathlib.Path(sys.argv[2]); prefix=z.namelist()[0].split('/')[0]+'/'
for info in z.infolist():
    if info.filename.startswith(prefix) and info.filename != prefix:
        target=out / info.filename[len(prefix):]
        if info.is_dir(): target.mkdir(parents=True, exist_ok=True)
        else:
            target.parent.mkdir(parents=True, exist_ok=True); target.write_bytes(z.read(info))
            if '/bin/' in info.filename or info.filename.endswith('/mvn'): target.chmod(target.stat().st_mode | 0o111)
PY
fi
exec "$MVN" "$@"
