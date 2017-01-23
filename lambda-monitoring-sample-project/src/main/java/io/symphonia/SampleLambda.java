package io.symphonia;

import com.codahale.metrics.Counter;
import io.symphonia.lambda.annotations.CloudwatchLogGroup;
import io.symphonia.lambda.annotations.CloudwatchMetric;
import io.symphonia.lambda.metrics.LambdaMetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleLambda {

    private static Logger LOG = LoggerFactory.getLogger(SampleLambda.class);

    @CloudwatchLogGroup("/aws/lambda/sample-lambda") // Must match AWS Lambda config
    private class Metrics extends LambdaMetricSet {

        @CloudwatchMetric // Metric name defaults to field name ("fooCounter")
        Counter fooCounter = new Counter();

        @CloudwatchMetric("myBarCounter") // Metric name overridden
        Counter barCounter = new Counter();
    }

    public void handler(String input) {
        Metrics metrics = new Metrics();

        if (input != null) {
            for (String part : input.split(" ")) {
                if ("foo".equalsIgnoreCase(part)) {
                    metrics.fooCounter.inc();
                } else if ("bar".equalsIgnoreCase(part)) {
                    metrics.barCounter.inc();
                }
            }
        }

        metrics.report(LOG);
    }
}