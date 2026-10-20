#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ -f taioba-web-1.0.0.jar ]; then
  exec java -jar taioba-web-1.0.0.jar
else
  exec java -jar target/taioba-web-1.0.0.jar
fi
