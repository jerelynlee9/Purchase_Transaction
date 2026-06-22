#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Wrapper startup script for POSIX platforms
# ----------------------------------------------------------------------------

# Optional OS specific env overrides
if [ -f "$HOME/.mavenrc" ] ; then
  . "$HOME/.mavenrc"
fi

# Discover Base Directory
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`" >/dev/null
APP_DIR="`pwd`"
cd "$SAVED" >/dev/null

# Clean up variables
CLASSPATH=""
WRAPPED_MAIN="org.apache.maven.wrapper.MavenWrapperMain"

# Locate Java executable
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  elif [ -x "$JAVA_HOME/bin/java" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
  fi
else
  JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

WRAPPER_JAR="$APP_DIR/.mvn/wrapper/maven-wrapper.jar"

# Execution logic
if [ -r "$WRAPPER_JAR" ]; then
    exec "$JAVACMD" \
      -classpath "$WRAPPER_JAR" \
      "-Dmaven.multiModuleProjectDirectory=$APP_DIR" \
      $WRAPPED_MAIN "$@"
else
    echo "Wrapper JAR not found. Falling back to global 'mvn' command..."
    if command -v mvn >/dev/null 2>&1; then
        exec mvn "$@"
    else
        echo "Error: Neither maven-wrapper.jar nor system 'mvn' was found." >&2
        exit 1
    fi
fi