#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
javac --release 17 -d build src/*.java test/*.java
java -cp build OrderTimeline
java -cp build TimelineFixtures
java -cp build IngestionFixtures
java -cp build FailureScenarios
printf '%s\n' 'PASS local ingestion, restart, cited mock summary and failure checks'
