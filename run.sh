#!/usr/bin/env bash
# Compiles the project into out/ and launches the CLI.
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.hospital.Main
