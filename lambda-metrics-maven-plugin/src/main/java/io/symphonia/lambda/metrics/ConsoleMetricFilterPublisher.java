package io.symphonia.lambda.metrics;

import com.google.common.base.Joiner;

import java.util.List;

public class ConsoleMetricFilterPublisher implements MetricFilterPublisher {

    @Override
    public void publishMetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, List<String> metricValues) {
        System.out.println(String.format("logGroupName = [%s], fullMetricName = [%s], pattern = [%s], values = [%s]",
                logGroupName, fullMetricName, filterPatternFormat, Joiner.on(",").join(metricValues)));
    }
}
