package io.symphonia.lambda.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import io.symphonia.lambda.annotations.CloudwatchMetric;
import io.symphonia.lambda.annotations.CloudwatchNamespace;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class LambdaMetricSetTest {

    private class TestLambdaMetricSet extends LambdaMetricSet {

        @CloudwatchMetric
        protected Counter fooCounter = new Counter();

        @CloudwatchMetric("myBarCounter")
        protected Counter barCounter = new Counter();

        @CloudwatchNamespace("test.namespace")
        @CloudwatchMetric
        protected Counter bazCounter = new Counter();

        @CloudwatchNamespace("test.namespace")
        @CloudwatchMetric("myBingCounter")
        protected Counter bingCounter = new Counter();
    }

    @CloudwatchNamespace("test.namespace")
    private class NamespaceTestLambdaMetricSet extends LambdaMetricSet {

        @CloudwatchMetric
        protected Counter fooCounter = new Counter();

        @CloudwatchMetric
        protected Gauge jvmUptime = JVM_UPTIME;
    }

    @Test
    public void testMetricSetAnnotations() {
        Map<String, Metric> metrics = new TestLambdaMetricSet().getMetrics();

        // Prove that the defaults fall back to the classname as namespace, and field name as metric name.
        assertThat(metrics.get("io.symphonia.lambda.metrics.LambdaMetricSetTest.TestLambdaMetricSet/fooCounter"),
                instanceOf(Counter.class));

        // Prove we can override the name via the annotation
        assertThat(metrics.get("io.symphonia.lambda.metrics.LambdaMetricSetTest.TestLambdaMetricSet/myBarCounter"),
                instanceOf(Counter.class));

        // Prove we can override the namespace via the annotation
        assertThat(metrics.get("test.namespace/bazCounter"), instanceOf(Counter.class));

        // Prove we can override the namespace and name via the annotation
        assertThat(metrics.get("test.namespace/myBingCounter"), instanceOf(Counter.class));

        // Prove we can override the namespace via a class-level annotation
        assertThat(new NamespaceTestLambdaMetricSet().getMetrics().get("test.namespace/fooCounter"),
                instanceOf(Counter.class));
    }

    @Test
    public void testJvmMetrics() {
        NamespaceTestLambdaMetricSet lambdaMetricSet = new NamespaceTestLambdaMetricSet();
        // Base class JVM_UPTIME
        assertThat((Long) lambdaMetricSet.JVM_UPTIME.getValue(), greaterThanOrEqualTo(0L));
        // Child class annotated field
        assertThat((Long) ((Gauge) lambdaMetricSet.getMetrics().get("test.namespace/jvmUptime")).getValue(), greaterThanOrEqualTo(0L));
    }

    @Test
    public void testJvmInformationFields() {
        TestLambdaMetricSet lambdaMetricSet = new TestLambdaMetricSet();
        assertThat(lambdaMetricSet.JVM_VENDOR, not(Matchers.emptyString()));
        assertThat(lambdaMetricSet.JVM_NAME, not(Matchers.emptyString()));
    }

}