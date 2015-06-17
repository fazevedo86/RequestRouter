#!/bin/sh

# Set paths
RR_HOME=`dirname $0`
RR_JAR="${RR_HOME}/RequestRouter.jar"
RR_LOGBACK="${RR_HOME}/logback.xml"

# Set JVM options
JVM_OPTS=""
JVM_OPTS="$JVM_OPTS -server"
JVM_OPTS="$JVM_OPTS -XX:+UseParallelGC"

# Create a logback file if required
[ -f ${RR_LOGBACK} ] || cat <<EOF_LOGBACK >${RR_LOGBACK}
<configuration scan="true">
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%level [%logger:%thread] %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
    <logger name="pt.ulisboa.tecnico.amorphous" level=“ALL”/>
</configuration>
EOF_LOGBACK

echo "Starting RequestRouter..."
java ${JVM_OPTS} -Dlogback.configurationFile=${RR_LOGBACK} -jar ${RR_JAR}
