package io.symphonia.lambda.metrics;

import com.codahale.metrics.Metric;
import io.symphonia.lambda.annotations.CloudwatchMetric;
import io.symphonia.lambda.annotations.CloudwatchNamespace;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class MetricsUtils {

    public static HashMap<String, Field> findAnnotatedFields(Class<? extends LambdaMetricSet> clazz) {
        HashMap<String, Field> annotatedFields = new HashMap<>();

        CloudwatchNamespace classNamespaceAnnotation = clazz.getAnnotation(CloudwatchNamespace.class);
        String classNamespace = classNamespaceAnnotation != null && !classNamespaceAnnotation.value().isEmpty() ?
                classNamespaceAnnotation.value() : clazz.getCanonicalName();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            CloudwatchMetric metricAnnotation =
                    field.getAnnotation(CloudwatchMetric.class);

            if (metricAnnotation != null) {

                String metricName = !metricAnnotation.value().isEmpty() ? metricAnnotation.value() : field.getName();
                CloudwatchNamespace metricNamespaceAnnotation = field.getAnnotation(CloudwatchNamespace.class);
                String metricNamespace = metricNamespaceAnnotation != null && !metricNamespaceAnnotation.value().isEmpty() ?
                        metricNamespaceAnnotation.value() : classNamespace;
                String fullMetricName = String.format("%s/%s", metricNamespace, metricName);

                annotatedFields.put(fullMetricName, field);
            }
        }
        return annotatedFields;
    }

    public static HashMap<String, Metric> findAnnotatedMetrics(LambdaMetricSet metricSet) {
        HashMap<String, Metric> annotatedMetrics = new HashMap<>();
        for (Map.Entry<String, Field> entry : findAnnotatedFields(metricSet.getClass()).entrySet()) {
            Field field = entry.getValue();
            field.setAccessible(true);
            try {
                annotatedMetrics.put(entry.getKey(), (Metric) field.get(metricSet));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return annotatedMetrics;
    }
}
