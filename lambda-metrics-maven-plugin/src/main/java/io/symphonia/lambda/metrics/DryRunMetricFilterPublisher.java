package io.symphonia.lambda.metrics;

import org.apache.maven.plugin.logging.Log;

public class DryRunMetricFilterPublisher implements MetricFilterPublisher {

    private Log log;

    public DryRunMetricFilterPublisher(Log log) {
        this.log = log;
    }

    @Override
    public boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue) {
        String[] parts = fullMetricName.split("/");
        String metricName = String.format("%s-%s", parts[1], metricValue);
        log.info(String.format("[dry run] Removing metric filter [%s] from log group [%s]", metricName, logGroupName));
        return false;
    }

    @Override
    public boolean publishMetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, String metricValue) {
        String[] parts = fullMetricName.split("/");
        String metricName = String.format("%s-%s", parts[1], metricValue);

        log.info(String.format("[dry run] Publishing metric filter [%s] to log group [%s]", metricName, logGroupName));
        log.debug(String.format("[dry run] filterPatternFormat [%s]", filterPatternFormat));
        log.debug(String.format("[dry run] metricValue [%s]", metricValue));
        return false;
    }
}
