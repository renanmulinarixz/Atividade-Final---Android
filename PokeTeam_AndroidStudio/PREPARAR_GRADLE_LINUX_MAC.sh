#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
BASE="https://raw.githubusercontent.com/gradle/gradle/v8.11.1"
curl -fL "$BASE/gradle/wrapper/gradle-wrapper.jar" -o gradle/wrapper/gradle-wrapper.jar
curl -fL "$BASE/gradlew" -o gradlew
curl -fL "$BASE/gradlew.bat" -o gradlew.bat
chmod +x gradlew
echo "Gradle Wrapper preparado."
