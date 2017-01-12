package io.symphonia.lambda.metrics;

public interface MetricFilterPublisher {
    boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue);

    boolean publishMetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, String metricValues);
}
