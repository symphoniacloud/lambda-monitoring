package io.symphonia.lambda.logging;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import io.symphonia.lambda.logging.MetricBlockFilter;
import org.junit.Test;
import org.slf4j.MarkerFactory;

import static io.symphonia.lambda.logging.MetricPassFilter.METRIC_MARKER;
import static org.junit.Assert.assertEquals;

public class MetricBlockFilterTest {

    @Test
    public void testPassesIfNotStarted() throws Exception {
        MetricBlockFilter filter = new MetricBlockFilter();
        assertEquals(FilterReply.NEUTRAL, filter.decide(new LoggingEvent()));
    }

    @Test
    public void testBlocksMetricMarker() {
        LoggingEvent event = new LoggingEvent();
        event.setMarker(METRIC_MARKER);
        MetricBlockFilter filter = new MetricBlockFilter();
        filter.start();
        assertEquals(FilterReply.DENY, filter.decide(event));
    }

    @Test
    public void testPassesNonMetricMarker() {
        LoggingEvent event = new LoggingEvent();
        event.setMarker(MarkerFactory.getMarker("FOO"));
        MetricBlockFilter filter = new MetricBlockFilter();
        filter.start();
        assertEquals(FilterReply.NEUTRAL, filter.decide(event));
    }

    @Test
    public void testPassesNoMarker() {
        LoggingEvent event = new LoggingEvent();
        MetricBlockFilter filter = new MetricBlockFilter();
        filter.start();
        assertEquals(FilterReply.NEUTRAL, filter.decide(event));
    }

}