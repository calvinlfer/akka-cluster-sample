import ch.qos.logback.core.*
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender(name="CONSOLE", clazz=ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "name=akka-cluster-sample date=%date{ISO8601} level=%level actor=%X{akkaSource} message=%msg\n"
    }
}

logger(name="akka", level=INFO)
logger(name="com.github.experiments.actors", level=INFO)

root(level=INFO, appenderNames=["CONSOLE"])
