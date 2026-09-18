#!/usr/bin/env bash
# Compiles the project and runs the validation test suite.
set -e
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.hospital.test.HospitalTest
