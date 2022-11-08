package io.symphonia.lambda.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.jvm.JvmAttributeGaugeSet;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Slf4jReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.symphonia.lambda.logging.MetricPassFilter.METRIC_MARKER;

@SuppressWarnings("WeakerAccess")
public abstract class LambdaMetricSet implements MetricSet {

    private JvmAttributeGaugeSet jvmAttributeGaugeSet = new JvmAttributeGaugeSet();

    public Gauge JVM_UPTIME = (Gauge) jvmAttributeGaugeSet.getMetrics().get("uptime");

    public String JVM_VENDOR = (String) ((Gauge) jvmAttributeGaugeSet.getMetrics().get("vendor")).getValue();

    public String JVM_NAME = (String) ((Gauge) jvmAttributeGaugeSet.getMetrics().get("name")).getValue();

    private Map<String, Metric> metrics = new HashMap<>();

    private MetricRegistry registry = new MetricRegistry();

    private Slf4jReporter reporter;

    @Override
    public Map<String, Metric> getMetrics() {
        metrics.putAll(MetricsUtils.findAnnotatedMetrics(this));
        return Collections.unmodifiableMap(metrics);
    }

    public void report() {
        report(LoggerFactory.getLogger(this.getClass().getName()));
    }

    public void report(Logger logger) {
        getReporter(logger).report();
    }

    private MetricRegistry getRegistry() {
        if (registry.getMetrics().isEmpty()) {
            registry = new MetricRegistry();
        }
        registry.registerAll(this);
        return registry;
    }

    private Slf4jReporter getReporter(Logger logger) {
        if (reporter == null) {
            reporter = Slf4jReporter.forRegistry(getRegistry())
                    .markWith(METRIC_MARKER)
                    .outputTo(logger)
                    .build();
        }
        return reporter;
    }

}