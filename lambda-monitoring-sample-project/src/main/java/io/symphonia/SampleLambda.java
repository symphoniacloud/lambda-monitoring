package io.symphonia;

import com.codahale.metrics.Counter;
import io.symphonia.lambda.annotations.CloudwatchLogGroup;
import io.symphonia.lambda.annotations.CloudwatchMetric;
import io.symphonia.lambda.metrics.LambdaMetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleLambda {

    private static Logger LOG = LoggerFactory.getLogger(SampleLambda.class);

    @CloudwatchLogGroup("/aws/lambda/myLambda")
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