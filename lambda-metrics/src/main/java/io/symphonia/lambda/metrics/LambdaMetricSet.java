package io.symphonia.lambda.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class LambdaMetricSet implements MetricSet {

    private JvmAttributeGaugeSet jvmAttributeGaugeSet = new JvmAttributeGaugeSet();

    public Gauge JVM_UPTIME = (Gauge) jvmAttributeGaugeSet.getMetrics().get("uptime");

    public String JVM_VENDOR = (String) ((Gauge) jvmAttributeGaugeSet.getMetrics().get("vendor")).getValue();

    public String JVM_NAME = (String) ((Gauge) jvmAttributeGaugeSet.getMetrics().get("name")).getValue();

    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(MetricsUtils.findAnnotatedMetrics(this));
    }

}