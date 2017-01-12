package io.symphonia.lambda.logging;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import io.symphonia.lambda.logging.MetricPassFilter;
import org.junit.Test;
import org.slf4j.MarkerFactory;

import static io.symphonia.lambda.logging.MetricPassFilter.METRIC_MARKER;
import static org.junit.Assert.assertEquals;

public class MetricPassFilterTest {

    @Test
    public void testPassesIfNotStarted() throws Exception {
        MetricPassFilter filter = new MetricPassFilter();
        assertEquals(FilterReply.NEUTRAL, filter.decide(new LoggingEvent()));
    }

    @Test
    public void testPassesMetricMarker() {
        LoggingEvent event = new LoggingEvent();
        event.setMarker(METRIC_MARKER);
        MetricPassFilter filter = new MetricPassFilter();
        filter.start();
        assertEquals(FilterReply.NEUTRAL, filter.decide(event));
    }

    @Test
    public void testBlocksNonMetricMarker() {
        LoggingEvent event = new LoggingEvent();
        event.setMarker(MarkerFactory.getMarker("FOO"));
        MetricPassFilter filter = new MetricPassFilter();
        filter.start();
        assertEquals(FilterReply.DENY, filter.decide(event));
    }

    @Test
    public void testBlocksNoMarker() {
        LoggingEvent event = new LoggingEvent();
        MetricPassFilter filter = new MetricPassFilter();
        filter.start();
        assertEquals(FilterReply.DENY, filter.decide(event));
    }

}