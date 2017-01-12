package io.symphonia.lambda.metrics;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteMetricFilterRequest;
import com.amazonaws.services.logs.model.MetricTransformation;
import com.amazonaws.services.logs.model.PutMetricFilterRequest;
import com.amazonaws.services.logs.model.ResourceNotFoundException;
import org.apache.maven.plugin.logging.Log;

public class CloudwatchMetricFilterPublisher implements MetricFilterPublisher {

    private AWSLogs client;

    private Log log;

    public CloudwatchMetricFilterPublisher(Log log) {
        // http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default
        this.client = AWSLogsClientBuilder.defaultClient();
        this.log = log;
    }

    @Override
    public boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue) {
        String[] parts = fullMetricName.split("/");
        String metricName = String.format("%s-%s", parts[1], metricValue);

        log.info(String.format("Removing metric filter [%s] from log group [%s]", metricName, logGroupName));

        DeleteMetricFilterRequest request = new DeleteMetricFilterRequest()
                .withLogGroupName(logGroupName)
                .withFilterName(metricName);

        try {
            client.deleteMetricFilter(request);
            return true;
        } catch (ResourceNotFoundException e) {
            log.warn(String.format("Did not find metric filter [%s] in log group [%s]", metricName, logGroupName));
        }

        return false;
    }

    @Override
    public boolean publishMetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, String metricValue) {
        String[] parts = fullMetricName.split("/");
        String metricNamespace = parts[0];
        String metricName = String.format("%s-%s", parts[1], metricValue);
        String filterPattern = String.format(filterPatternFormat, fullMetricName);

        log.info(String.format("Publishing metric filter [%s] to log group [%s]", metricName, logGroupName));
        log.debug(String.format("filterPatternFormat [%s]", filterPatternFormat));
        log.debug(String.format("filterPattern [%s]", filterPattern));
        log.debug(String.format("metricValue [%s]", metricValue));
        log.debug(String.format("metricName [%s]", metricName));

        MetricTransformation metricTransformation = new MetricTransformation()
                .withMetricNamespace(metricNamespace)
                .withMetricName(metricName)
                .withMetricValue(metricValue);

        PutMetricFilterRequest request = new PutMetricFilterRequest()
                .withLogGroupName(logGroupName)
                .withFilterName(metricName)
                .withFilterPattern(filterPattern)
                .withMetricTransformations(metricTransformation);

        client.putMetricFilter(request);
        return true;
    }

}
