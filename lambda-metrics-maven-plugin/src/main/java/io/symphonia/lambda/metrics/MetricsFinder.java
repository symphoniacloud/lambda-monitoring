package io.symphonia.lambda.metrics;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Add prefix/package filtering

public class MetricsFinder {

    private Collection<String> classpathElements;

    public MetricsFinder(Collection<String> classpathElements) {
        this.classpathElements = classpathElements;
    }

    public Map<String, Field> find() throws Exception {

        List<URL> urls = new ArrayList<>();
        for (String element : classpathElements) {
            URL url = new File(element).toURI().toURL();
            urls.add(url);
        }

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(urls)
                        .addClassLoader(new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader()))
                        .setScanners(new SubTypesScanner()));

        Map<String, Field> metricFields = new HashMap<>();
        for (Class<? extends LambdaMetricSet> type : reflections.getSubTypesOf(LambdaMetricSet.class)) {
            metricFields.putAll(MetricsUtils.findAnnotatedFields(type));
        }

        return metricFields;
    }

}
