# lambda-metrics



## Quick Start

1. **Add the `io.symphonia/lambda-metrics` dependency to your project**

   For Maven projects:
   ```xml
   <dependency>
     <groupId>io.symphonia</groupId>
     <artifactId>lambda-metrics</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```
   
   The vanilla, no-classifier version of the library includes a default Logback configuration optimized for metrics 
   collection.
   
1. **Start collecting metrics**

    ```java
    package io.symphonia.lambda;
    
    import com.codahale.metrics.Counter;
    import io.symphonia.lambda.annotations.CloudwatchMetric;
    import io.symphonia.lambda.metrics.LambdaMetricSet;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    public class TestLambda {
    
        Logger LOG = LoggerFactory.getLogger(TestLambda.class);
    
        private class Metrics extends LambdaMetricSet {
         
            @CloudwatchMetric
            Counter fooCounter = new Counter();
        }
    
        public void handler(String input) {
            Metrics metrics = new Metrics();
    
            if (input != null) {
                for (String part : input.split(" ")) {
                    if ("foo".equalsIgnoreCase(part)) {
                        metrics.fooCounter.inc();
                    }
                }
            }
    
            metrics.report(LOG);
        }
    }
    ```
    
1. **Publish Metric Filters**

Use the [`lambda-metrics-maven-plugin`](/lambda-metrics-maven-plugin) to publish Cloudwatch Logs Metric Filters, which 
will extract your annotated metrics out of Cloudwatch Logs and push them to Cloudwatch Metrics. See that project for 
more details.
    
## FAQ

#### 1. Which Codahale Metrics are available in `lambda-metrics`?

All of them; Counters, Gauges, Meters, Histograms, and Timers. Note that some are more useful than others
within the limited scope and runtime of a Lambda invocation.

#### 1. Are there any internal JVM metrics available?

Yes. `LambdaMetricSet` makes the JVM uptime, vendor string, and name available as public instance fields. For example,
to log a JVM uptime metric, use the following in your child Metrics class:

    ```java
    @CloudwatchMetric
    Gauge jvmUptime = JVM_UPTIME;
    ```
    
You can also use Codahale's [`metrics-jvm` library](http://metrics.dropwizard.io/3.1.0/manual/jvm/), which exposes a 
wide range of internal JVM metrics.
    
## TODO


