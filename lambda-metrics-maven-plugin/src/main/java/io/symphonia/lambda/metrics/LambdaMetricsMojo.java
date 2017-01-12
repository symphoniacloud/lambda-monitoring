package io.symphonia.lambda.metrics;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.logs.AWSLogsClient;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mojo(name = "lambda-metrics", requiresDependencyResolution = ResolutionScope.COMPILE)
public class LambdaMetricsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "us-east-1", required = true)
    private String regionName;

    @Parameter(required = true)
    private String cloudwatchLogGroupName;

    public static Map<String, String> DEFAULT_FILTER_PATTERN_MAP = new HashMap<String, String>() {{
        put("COUNTER", "[datetime,request_id,level=METRIC,logger,type_label,type=COUNTER,name_label,name=\"%s\",count_label,count]");
        put("GAUGE", "[datetime,request_id,level=METRIC,logger,type_label,type=GAUGE,name_label,name=\"%s\",value_label,value]");
        put("METER", "[datetime,request_id,level=METRIC,logger,type_label,type=METER,name_label,name=\"%s\",count_label,count,mean_rate_label,mean_rate,m1_label,m1,m5_label,m5,m15_label,m15,rate_unit_label,rate_unit]");
        put("HISTOGRAM", "[datetime,request_id,level=METRIC,logger,type_label,type=HISTOGRAM,name_label,name=\"%s\",count_label,count,min_label,min,max_label,max,mean_label,mean,stddev_label,stddev,median_label,median,p75_label,p75,p95_label,p95,p98_label,p98,p99_label,p99,p999_label,p999]");
        put("TIMER", "[datetime,request_id,level=METRIC,logger,type_label,type=TIMER,name_label,name=\"%s\",count_label,count,min_label,min,max_label,max,mean_label,mean,stddev_label,stddev,median_label,median,p75_label,p75,p95_label,p95,p98_label,p98,p99_label,p99,p999_label,p999,mean_rate_label,rate,m1_label,m1,m5_label,m5,m15_label,m15,rate_unit_label,unit,duration_unit_label,unit]");
    }};

    @Parameter(required = false)
    private Map<String, String> filterPatternMap = DEFAULT_FILTER_PATTERN_MAP;

    public static Map<String, List<String>> DEFAULT_METRIC_VALUE_MAP = new HashMap<String, List<String>>() {{
        put("COUNTER", Collections.singletonList("count"));
        put("GAUGE", Collections.singletonList("value"));
        put("METER", Arrays.asList("mean_rate", "m1", "m5", "m15"));
        put("HISTOGRAM", Arrays.asList("min", "max", "mean", "stddev", "median",
                "p75", "p95", "p98", "p99", "p999"));
        put("TIMER", Arrays.asList("min", "max", "mean", "stddev", "median",
                "p75", "p95", "p98", "p99", "p999", "rate", "m1", "m5", "m15"));
    }};

    @Parameter(required = false)
    private Map<String, List<String>> metricValueMap = DEFAULT_METRIC_VALUE_MAP;

    public LambdaMetricsMojo() {
    }

    public void execute() throws MojoExecutionException, MojoFailureException {


        Region region = Region.getRegion(Regions.fromName(regionName));
        AWSLogsClient client = new AWSLogsClient().withRegion(region);
        MetricFilterPublisher publisher = new AwsMetricFilterPublisher(client);

        Set<String> classpathElements = new HashSet<>();
        try {
            classpathElements.addAll(project.getRuntimeClasspathElements());
            classpathElements.addAll(project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Dependency resolution required", e);
        }

        try {
            for (Map.Entry<String, Field> entry : new MetricsFinder(classpathElements).find().entrySet()) {
                String fullMetricName = entry.getKey();
                String metricType = entry.getValue().getType().getSimpleName().toUpperCase();
                publisher.publishMetricFilter(cloudwatchLogGroupName, fullMetricName,
                        filterPatternMap.get(metricType), metricValueMap.get(metricType));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error finding metrics", e);
        }


    }
}
