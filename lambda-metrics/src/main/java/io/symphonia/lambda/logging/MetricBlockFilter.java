package io.symphonia.lambda.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

public class MetricBlockFilter extends Filter<ILoggingEvent> {

    // Pass all logging events that don't include METRIC marker, deny all others

    @Override
    public FilterReply decide(ILoggingEvent event) {
        Marker eventMarker = event.getMarker();
        if (isStarted() && eventMarker != null && eventMarker.contains(MetricPassFilter.METRIC_MARKER)) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }
}
