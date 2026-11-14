#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

rm -rf build
mkdir -p build/classes build/stubs dist

find stubs -name '*.java' > build/stub-sources.txt
javac --release 17 -encoding UTF-8 -d build/stubs @build/stub-sources.txt

find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -cp build/stubs -sourcepath src/main/java -d build/classes @build/sources.txt
cp -R src/main/resources/. build/classes/

jar --create --file dist/forbric-boomer-1.0.0.jar \
  --main-class io.github.lcxws.forbricboomer.Boomer \
  -C build/classes .

echo "built dist/forbric-boomer-1.0.0.jar"

