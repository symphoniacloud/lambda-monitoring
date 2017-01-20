package io.symphonia.lambda.metrics;

import org.apache.maven.plugin.logging.Log;

import java.util.Collection;
import java.util.List;

public class DryRunMetricFilterPublisher implements MetricFilterPublisher {

    private Log log;

    public DryRunMetricFilterPublisher(Log log) {
        this.log = log;
    }

    @Override
    public boolean publishMetricFilter(MetricFilter metricFilter) {
        log.info(String.format("[dry run] Publishing metric filter [%s] to log group [%s]",
                metricFilter.getMetricName(), metricFilter.getLogGroupName()));
        log.debug(String.format("[dry run] metricValue [%s]", metricFilter.getMetricValue()));
        return false;
    }

    @Override
    public int publishMetricFilters(Collection<MetricFilter> metricFilters) {
        for (MetricFilter metricFilter : metricFilters) {
            publishMetricFilter(metricFilter);
        }
        return 0;
    }

    @Override
    public int removeMetricFilters(List<MetricFilter> metricFilters) {
        return 0;
    }

    @Override
    public boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue) {
        return removeMetricFilter(new MetricFilter(logGroupName, fullMetricName, metricValue));
    }

    @Override
    public boolean removeMetricFilter(MetricFilter metricFilter) {
        log.info(String.format("[dry run] Removing metric filter [%s] from log group [%s]",
                metricFilter.getMetricName(), metricFilter.getLogGroupName()));
        return false;
    }

}
