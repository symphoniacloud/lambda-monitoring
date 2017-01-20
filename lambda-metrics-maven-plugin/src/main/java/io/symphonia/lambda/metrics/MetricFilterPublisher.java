package io.symphonia.lambda.metrics;

import java.util.Collection;
import java.util.List;

public interface MetricFilterPublisher {
    boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue);

    boolean publishMetricFilter(MetricFilter metricFilter);

    int publishMetricFilters(Collection<MetricFilter> metricFilters);

    int removeMetricFilters(List<MetricFilter> metricFilters);

    boolean removeMetricFilter(MetricFilter metricFilter);
}
