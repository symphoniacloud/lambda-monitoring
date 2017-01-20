package io.symphonia.lambda.metrics;

import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.MetricFilterMatchRecord;
import com.amazonaws.services.logs.model.TestMetricFilterRequest;
import com.amazonaws.services.logs.model.TestMetricFilterResult;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import io.symphonia.lambda.annotations.CloudwatchMetric;
import io.symphonia.lambda.annotations.CloudwatchNamespace;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.symphonia.lambda.metrics.PublishMetricFiltersMojo.COMPLETE_FILTER_PATTERN_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CloudwatchMetricFilterTest {

    @CloudwatchNamespace("test.namespace")
    private class TestMetricSet extends LambdaMetricSet {
        @CloudwatchMetric
        Counter testCounter = new Counter();

        @CloudwatchMetric
        Gauge<Long> testGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return 42L;
            }
        };

        @CloudwatchMetric
        Meter testMeter = new Meter();

        @CloudwatchMetric
        Histogram testHistogram = new Histogram(new UniformReservoir());

        @CloudwatchMetric
        Timer testTimer = new Timer();

    }


    @Ignore
    @Test
    public void testMetricFilters() {

        List<String> lines;

        PrintStream original = System.out;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream, true));

            Logger logger = LoggerFactory.getLogger("TEST-LOGGER");
            logger.info("TEST-MESSAGE");

            TestMetricSet metricSet = new TestMetricSet();
            MetricRegistry registry = new MetricRegistry();
            registry.registerAll(metricSet);

            metricSet.testCounter.inc();
            metricSet.testMeter.mark(1L);
            metricSet.testHistogram.update(1L);
            metricSet.testTimer.update(1L, TimeUnit.MINUTES);

            Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                    .markWith(MarkerFactory.getMarker("METRIC"))
                    .outputTo(logger)
                    .build();

            reporter.report();
            lines = Arrays.asList(outputStream.toString().split("\\n"));
        } finally {
            System.setOut(original);
        }

        assertNotNull(lines);

        AWSLogsClient client = new AWSLogsClient();

        // TODO: Loop for each kind of metric

        String metricFilterPattern =
                String.format(COMPLETE_FILTER_PATTERN_MAP.get("COUNTER"), "test.namespace/testCounter");

        TestMetricFilterRequest request = new TestMetricFilterRequest()
                .withFilterPattern(metricFilterPattern)
                .withLogEventMessages(lines);

        TestMetricFilterResult result = client.testMetricFilter(request);

        MetricFilterMatchRecord matchRecord = result.getMatches().get(0);
        assertEquals("test.namespace/testCounter", matchRecord.getExtractedValues().get("$name"));
        assertEquals("1", matchRecord.getExtractedValues().get("$count"));

        MetricFilterMatchRecord matchRecord2 = result.getMatches().get(1);
        assertEquals("test.namespace/testGauge", matchRecord2.getExtractedValues().get("$name"));
        assertEquals("42", matchRecord2.getExtractedValues().get("$value"));
    }

}
