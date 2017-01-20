package io.symphonia.lambda.metrics;

public class MetricFilter {

    private final String logGroupName;
    private final String metricValue;
    private final String metricNamespace;
    private final String metricName;
    private final String filterPattern;

    public MetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, String metricValue) {
        this.logGroupName = logGroupName;
        this.metricValue = metricValue;

        String[] parts = fullMetricName.split("/");
        this.metricNamespace = parts[0];
        this.metricName = String.format("%s-%s", parts[1], metricValue);
        this.filterPattern = String.format(filterPatternFormat, fullMetricName);
    }

    public MetricFilter(String logGroupName, String fullMetricName, String metricValue) {
        this.logGroupName = logGroupName;
        this.metricValue = metricValue;

        String[] parts = fullMetricName.split("/");
        this.metricNamespace = parts[0];
        this.metricName = String.format("%s-%s", parts[1], metricValue);

        filterPattern = null;
    }

    public String getLogGroupName() {
        return logGroupName;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public String getMetricNamespace() {
        return metricNamespace;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFilterPattern() {
        return filterPattern;
    }

}
