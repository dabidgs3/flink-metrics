# Example used in a local host
## Parameters
```
-j JSON file directory, the program is able to read all the json files placed in the directory, and constantly check its modification and update.
-e Loop execution enabled
-s Time slot for querying to JMX server (granularity), expressed in seconds 
```
## Debug Command
```
mvn -e -X exec:java -Dexec.mainClass="berlin.bbdc.inet.flinkjmx.JmxTransformer" -Dexec.args="-j /var/lib/jmxtrans/ -e -s 3"
```

## JMXFlinkTransformer Configuration

```json
{
  "servers" : [ {
    "port" : "9010",
    "host" : "localhost",
    "queries" : [ {
      "obj" : "org.apache.*:*",
      "outputWriters" : [ {
        "@class" : "berlin.bbdc.inet.flinkjmx.output.FileWriter",
          "filepath" : "/home/dgu/developer/flink-metrics/wordcountMetrics/result.out"
      },{
        "@class" : "berlin.bbdc.inet.flinkjmx.output.GraphiteWriterFactory",
          "port" : 2003,
          "host" : "localhost"
      } ]
    } ]
  } ]
}
```
## Reading File 
```
tail -f /home/dgu/developer/flink-metrics/wordcountMetrics/result.out
```
To avoid TCP Connection Exceptions in Graphite, the following configuration could be used:
```json
{
  "servers" : [ {
    "port" : "9010",
    "host" : "localhost",
    "queries" : [ {
      "obj" : "org.apache.*:*",
      "outputWriters" : [ {
        "@class" : "berlin.bbdc.inet.flinkjmx.output.FileWriter",
          "filepath" : "/home/dgu/developer/flink-metrics/wordcountMetrics/result.out"
      }]
    } ]
  } ]
}
```

## Flink Configuration
In the next configuration metrics scopes may be vanished.
```

#=================================
#
#METRICS
#
#=================================
metrics.scope.jm: localhost.myhost
metrics.scope.jm.job: localhost.jobmanager.myjm
metrics.scope.tm: localhost.taskmanager.mytm
metrics.scope.tm.job: localhost.mytm.myjob
metrics.scope.task: localhost.taskmanager.mytm.myjob.mytask.idtask

metrics.reporters: my_jmx_reporter
# #specifying the class for the internal JMX reporter
# #using the default flink port range for the default flink reporter
metrics.reporter.my_jmx_reporter.class: org.apache.flink.metrics.jmx.JMXReporter
metrics.reporter.my_jmx_reporter.port: 9010-9017

```
