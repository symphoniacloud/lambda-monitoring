package io.symphonia.lambda.metrics;

import java.util.List;

public interface MetricFilterPublisher {
    void publishMetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, List<String> metricValues);
}
