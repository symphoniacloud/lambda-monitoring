package io.symphonia.lambda.metrics;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "publish")
public class PublishMetricFiltersMojo extends AbstractMojo {

    public static Map<String, String> DEFAULT_FILTER_PATTERN_MAP = new HashMap<String, String>() {{
        put("COUNTER", "[datetime,request_id,level=METRIC,logger,type_label,type=COUNTER,name_label,name=\"%s\",count_label,count]");
        put("GAUGE", "[datetime,request_id,level=METRIC,logger,type_label,type=GAUGE,name_label,name=\"%s\",value_label,value]");
        put("METER", "[datetime,request_id,level=METRIC,logger,type_label,type=METER,name_label,name=\"%s\",count_label,count,mean_rate_label,mean_rate,m1_label,m1,m5_label,m5,m15_label,m15,rate_unit_label,rate_unit]");
        put("HISTOGRAM", "[datetime,request_id,level=METRIC,logger,type_label,type=HISTOGRAM,name_label,name=\"%s\",count_label,count,min_label,min,max_label,max,mean_label,mean,stddev_label,stddev,median_label,median,p75_label,p75,p95_label,p95,p98_label,p98,p99_label,p99,p999_label,p999]");
        put("TIMER", "[datetime,request_id,level=METRIC,logger,type_label,type=TIMER,name_label,name=\"%s\",count_label,count,min_label,min,max_label,max,mean_label,mean,stddev_label,stddev,median_label,median,p75_label,p75,p95_label,p95,p98_label,p98,p99_label,p99,p999_label,p999,mean_rate_label,rate,m1_label,m1,m5_label,m5,m15_label,m15,rate_unit_label,unit,duration_unit_label,unit]");
    }};

    public static Map<String, List<String>> DEFAULT_METRIC_VALUE_MAP = new HashMap<String, List<String>>() {{
        put("COUNTER", Collections.singletonList("count"));
        put("GAUGE", Collections.singletonList("value"));
        put("METER", Arrays.asList("mean_rate", "m1", "m5", "m15"));
        put("HISTOGRAM", Arrays.asList("min", "max", "mean", "stddev", "median",
                "p75", "p95", "p98", "p99", "p999"));
        put("TIMER", Arrays.asList("min", "max", "mean", "stddev", "median",
                "p75", "p95", "p98", "p99", "p999", "rate", "m1", "m5", "m15"));
    }};

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "dryRun", defaultValue = "false", required = true)
    private boolean dryRun;

    @Parameter(defaultValue = "", required = true)
    private String cloudwatchLogGroupName;

    @Parameter(required = false)
    private Map<String, String> filterPatternMap = DEFAULT_FILTER_PATTERN_MAP;

    @Parameter(required = false)
    private Map<String, List<String>> metricValueMap = DEFAULT_METRIC_VALUE_MAP;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        checkConfiguration();

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

                if (!filterPatternMap.keySet().contains(metricType)) {
                    getLog().warn(String.format("Skipping metric field [%s] with unsupported type [%s]",
                            metricField.getName(), metricType));
                    break;
                }

                String filterPatternFormat = filterPatternMap.get(metricType);
                List<String> metricValues = metricValueMap.get(metricType);

                for (String metricValue : metricValues) {
                    if (publisher.publishMetricFilter(cloudwatchLogGroupName, fullMetricName,
                            filterPatternFormat, metricValue)) {
                        metricFilterCount++;
                    }
                }
            }
            getLog().info(String.format("Published [%d] metric filters.", metricFilterCount));
        } else {
            getLog().warn("Did not find any metric fields in classpath.");
        }

    }

    private void checkConfiguration() throws MojoFailureException {
        if (!filterPatternMap.keySet().containsAll(metricValueMap.keySet())
                || !metricValueMap.keySet().containsAll(filterPatternMap.keySet())) {
            throw new MojoFailureException("filterPatternMap and metricValueMap are inconsistent.");
        }
    }

}
