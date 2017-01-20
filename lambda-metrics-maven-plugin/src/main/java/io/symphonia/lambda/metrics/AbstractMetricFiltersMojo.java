package io.symphonia.lambda.metrics;

import io.symphonia.lambda.annotations.CloudwatchLogGroup;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMetricFiltersMojo extends AbstractMojo {
    public static Map<String, String> COMPLETE_FILTER_PATTERN_MAP = new HashMap<String, String>() {{
        put("COUNTER", "[datetime,request_id,level=METRIC,logger,type_label,type=COUNTER,name_label,name=\"%s\",count_label,count]");
        put("GAUGE", "[datetime,request_id,level=METRIC,logger,type_label,type=GAUGE,name_label,name=\"%s\",value_label,value]");
        put("METER", "[datetime,request_id,level=METRIC,logger,type_label,type=METER,name_label,name=\"%s\",count_label,count,mean_rate_label,mean_rate,...]");
        put("HISTOGRAM", "[datetime,request_id,level=METRIC,logger,type_label,type=HISTOGRAM,name_label,name=\"%s\",count_label,count,min_label,min,max_label,max,mean_label,mean,stddev_label,stddev,median_label,median,p75_label,p75,p95_label,p95,p98_label,p98,p99_label,p99,p999_label,p999]");
        put("TIMER", "[datetime,request_id,level=METRIC,logger,type_label,type=TIMER,name_label,name=\"%s\",count_label,count,min_label,min,max_label,max,mean_label,mean,stddev_label,stddev,median_label,median,p75_label,p75,p95_label,p95,p98_label,p98,p99_label,p99,p999_label,p999,mean_rate_label,mean_rate,...]");
    }};
    public static Map<String, List<String>> COMPLETE_METRIC_VALUE_MAP = new HashMap<String, List<String>>() {{
        put("COUNTER", Collections.singletonList("count"));
        put("GAUGE", Collections.singletonList("value"));
        put("METER", Arrays.asList("count", "mean_rate", "m1", "m5", "m15"));
        put("HISTOGRAM", Arrays.asList("count", "min", "max", "mean", "stddev", "median",
                "p75", "p95", "p98", "p99", "p999"));
        put("TIMER", Arrays.asList("count", "min", "max", "mean", "stddev", "median",
                "p75", "p95", "p98", "p99", "p999", "mean_rate", "m1", "m5", "m15"));
    }};
    public static Map<String, List<String>> REDUCED_METRIC_VALUE_MAP = new HashMap<String, List<String>>() {{
        put("COUNTER", Collections.singletonList("count"));
        put("GAUGE", Collections.singletonList("value"));
        put("METER", Collections.singletonList("mean_rate"));
        put("HISTOGRAM", Arrays.asList("min", "max", "mean", "p75", "p99", "p999"));
        put("TIMER", Arrays.asList("min", "max", "mean", "p75", "p99", "p999", "mean_rate"));
    }};

    @Parameter(required = false)
    public Map<String, String> filterPatternMap = COMPLETE_FILTER_PATTERN_MAP;

    @Parameter(required = false)
    public Map<String, List<String>> metricValueMap = COMPLETE_METRIC_VALUE_MAP;

    @Parameter(required = false)
    private String cloudwatchLogGroupName;

    protected List<MetricFilter> getMetricFilters(Map<String, Field> metricFields) {
        List<MetricFilter> metricFilters = new ArrayList<>();
        for (Map.Entry<String, Field> metricFieldEntry : metricFields.entrySet()) {
            String fullMetricName = metricFieldEntry.getKey();
            Field metricField = metricFieldEntry.getValue();
            String metricType = metricField.getType().getSimpleName().toUpperCase();

            if (!filterPatternMap.keySet().contains(metricType)) {
                getLog().warn(String.format("Skipping metric field [%s] with unsupported type [%s]",
                        metricField.getName(), metricType));
                break;
            }

            CloudwatchLogGroup logGroupAnnotation = metricField.getDeclaringClass().getAnnotation(CloudwatchLogGroup.class);
            String logGroup = logGroupAnnotation != null && !logGroupAnnotation.value().isEmpty() ?
                    logGroupAnnotation.value() : cloudwatchLogGroupName;

            if (logGroup == null || logGroup.isEmpty()) {
                getLog().warn(String.format("No log group found for metric field [%s].", metricField.getName()));
                break;
            }

            // TODO: Merge w/ default values, so overriding doesn't require complete respecification
            String filterPatternFormat = filterPatternMap.get(metricType);
            List<String> metricValues = metricValueMap.get(metricType);


            for (String metricValue : metricValues) {
                metricFilters.add(new MetricFilter(logGroup, fullMetricName, filterPatternFormat, metricValue));
            }
        }
        return metricFilters;
    }
}
