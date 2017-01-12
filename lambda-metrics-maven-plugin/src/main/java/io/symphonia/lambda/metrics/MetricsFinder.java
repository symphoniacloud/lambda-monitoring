package io.symphonia.lambda.metrics;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class MetricsFinder {

    private Set<URL> classpathUrls;

    public MetricsFinder(MavenProject project) throws MalformedURLException, DependencyResolutionRequiredException {
        this.classpathUrls = collectClasspathUrls(project);
    }

    public Map<String, Field> find() throws MalformedURLException {

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(classpathUrls)
                        .addClassLoader(new URLClassLoader(classpathUrls.toArray(new URL[0]), getClass().getClassLoader()))
                        .setScanners(new SubTypesScanner()));

        Map<String, Field> metricFields = new HashMap<>();
        for (Class<? extends LambdaMetricSet> type : reflections.getSubTypesOf(LambdaMetricSet.class)) {
            metricFields.putAll(MetricsUtils.findAnnotatedFields(type));
        }

        return metricFields;
    }

    private Set<URL> collectClasspathUrls(MavenProject project) throws DependencyResolutionRequiredException, MalformedURLException {
        Set<String> classpathElements = new HashSet<>();
        classpathElements.addAll(project.getRuntimeClasspathElements());
        classpathElements.addAll(project.getCompileClasspathElements());
        return collectClasspathUrls(classpathElements);
    }

    private Set<URL> collectClasspathUrls(Set<String> classpathElements) throws MalformedURLException {
        Set<URL> urls = new HashSet<>();
        for (String element : classpathElements) {
            URL url = new File(element).toURI().toURL();
            urls.add(url);
        }
        return urls;
    }

}
