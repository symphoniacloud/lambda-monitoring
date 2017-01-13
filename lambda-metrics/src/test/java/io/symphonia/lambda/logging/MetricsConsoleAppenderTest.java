package io.symphonia.lambda.logging;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import io.symphonia.lambda.annotations.CloudwatchMetric;
import io.symphonia.lambda.metrics.LambdaMetricSet;
import org.apache.log4j.MDC;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;

public class MetricsConsoleAppenderTest {

    private class TestMetricSet extends LambdaMetricSet {
        @CloudwatchMetric
        Counter testCounter = new Counter();
    }

    @Test
    public void testMetricLogger() {
        PrintStream original = System.out;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream, true));

            MDC.put("AWSRequestId", "AWS-REQUEST-ID");
            Logger logger = LoggerFactory.getLogger("TEST-LOGGER");

            MetricRegistry registry = new MetricRegistry();
            registry.registerAll(new TestMetricSet());

            Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                    .markWith(MarkerFactory.getMarker("METRIC"))
                    .outputTo(logger)
                    .build();

            reporter.report();

            assertThat(outputStream.toString(),
                    matchesPattern("^\\[[0-9\\-:\\. ]{23}\\] AWS-REQUEST-ID METRIC TEST-LOGGER type COUNTER name " +
                            TestMetricSet.class.getCanonicalName() + "/testCounter count 0\\n$"));
        } finally {
            System.setOut(original);
        }
    }
}
