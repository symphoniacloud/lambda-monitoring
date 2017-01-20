package io.symphonia.lambda.metrics;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteMetricFilterRequest;
import com.amazonaws.services.logs.model.MetricTransformation;
import com.amazonaws.services.logs.model.PutMetricFilterRequest;
import com.amazonaws.services.logs.model.ResourceNotFoundException;
import org.apache.maven.plugin.logging.Log;

import java.util.Collection;
import java.util.List;

public class CloudwatchMetricFilterPublisher implements MetricFilterPublisher {

    private AWSLogs client;

    private Log log;

    public CloudwatchMetricFilterPublisher(Log log) {
        // http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default
        this.client = AWSLogsClientBuilder.defaultClient();
        this.log = log;
    }

    @Override
    public boolean publishMetricFilter(MetricFilter metricFilter) {
        log.info(String.format("Publishing metric filter [%s] to log group [%s]",
                metricFilter.getMetricName(), metricFilter.getLogGroupName()));
        log.debug(String.format("filterPattern [%s]", metricFilter.getFilterPattern()));
        log.debug(String.format("metricValue [%s]", metricFilter.getMetricValue()));
        log.debug(String.format("metricName [%s]", metricFilter.getMetricName()));

        MetricTransformation metricTransformation = new MetricTransformation()
                .withMetricNamespace(metricFilter.getMetricNamespace())
                .withMetricName(metricFilter.getMetricName())
                .withMetricValue(String.format("$%s", metricFilter.getMetricValue()));

        client.putMetricFilter(new PutMetricFilterRequest()
                .withLogGroupName(metricFilter.getLogGroupName())
                .withFilterName(metricFilter.getMetricName())
                .withFilterPattern(metricFilter.getFilterPattern())
                .withMetricTransformations(metricTransformation));

        return true;
    }

    @Override
    public int publishMetricFilters(Collection<MetricFilter> metricFilters) {
        int published = 0;
        for (MetricFilter metricFilter : metricFilters) {
            if (publishMetricFilter(metricFilter)) {
                published++;
            }
        }
        return published;
    }

    @Override
    public int removeMetricFilters(List<MetricFilter> metricFilters) {
        int removed = 0;
        for (MetricFilter metricFilter : metricFilters) {
            if (removeMetricFilter(metricFilter)) {
                removed++;
            }
        }
        return removed;
    }

    @Override
    public boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue) {
        return removeMetricFilter(new MetricFilter(logGroupName, fullMetricName, metricValue));
    }


    @Override
    public boolean removeMetricFilter(MetricFilter metricFilter) {
        log.info(String.format("Removing metric filter [%s] from log group [%s]",
                metricFilter.getMetricName(), metricFilter.getLogGroupName()));

        DeleteMetricFilterRequest request = new DeleteMetricFilterRequest()
                .withLogGroupName(metricFilter.getLogGroupName())
                .withFilterName(metricFilter.getMetricName());

        try {
            client.deleteMetricFilter(request);
            return true;
        } catch (ResourceNotFoundException e) {
            log.warn(String.format("Did not find metric filter [%s] in log group [%s]",
                    metricFilter.getMetricName(), metricFilter.getLogGroupName()));
        }

        return false;
    }

}
