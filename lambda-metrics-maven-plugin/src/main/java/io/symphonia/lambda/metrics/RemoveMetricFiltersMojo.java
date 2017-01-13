package io.symphonia.lambda.metrics;

import io.symphonia.lambda.annotations.CloudwatchLogGroup;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.symphonia.lambda.metrics.PublishMetricFiltersMojo.COMPLETE_METRIC_VALUE_MAP;

@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "remove")
public class RemoveMetricFiltersMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "dryRun", defaultValue = "false", required = true)
    private boolean dryRun;

    @Parameter(required = false)
    private String cloudwatchLogGroupName;

    @Parameter(required = false)
    private Map<String, List<String>> metricValueMap = COMPLETE_METRIC_VALUE_MAP;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        MetricFilterPublisher publisher =
                dryRun ? new DryRunMetricFilterPublisher(getLog()) : new CloudwatchMetricFilterPublisher(getLog());

        // Get a map of fully-namespaced metric names mapped to the metric fields in classes that extend LambdaMetricSet
        Map<String, Field> metricFields = new HashMap<>();
        try {
            metricFields.putAll(new MetricsFinder(project).find());
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Could not scan classpath for metric fields", e);
        }

        if (!metricFields.isEmpty()) {
            getLog().info(String.format("Found [%d] metric fields in classpath.", metricFields.size()));

            int metricFilterCount = 0;
            for (Map.Entry<String, Field> metricFieldEntry : metricFields.entrySet()) {
                String fullMetricName = metricFieldEntry.getKey();
                Field metricField = metricFieldEntry.getValue();
                String metricType = metricField.getType().getSimpleName().toUpperCase();

                CloudwatchLogGroup logGroupAnnotation = metricField.getDeclaringClass().getAnnotation(CloudwatchLogGroup.class);
                String logGroup = logGroupAnnotation != null && !logGroupAnnotation.value().isEmpty() ?
                        logGroupAnnotation.value() : cloudwatchLogGroupName;

                if (logGroup == null || logGroup.isEmpty()) {
                    getLog().warn(String.format("No log group found for metric field [%s].", metricField.getName()));
                    break;
                }

                List<String> metricValues = metricValueMap.get(metricType);

                for (String metricValue : metricValues) {
                    if (publisher.removeMetricFilter(logGroup, fullMetricName, metricValue)) {
                        metricFilterCount++;
                    }
                }
            }
            getLog().info(String.format("Removed [%d] metric filters.", metricFilterCount));
        } else {
            getLog().warn("Did not find any metric fields in classpath.");
        }

    }

}
