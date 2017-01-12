package io.symphonia.lambda.metrics;

import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.MetricTransformation;
import com.amazonaws.services.logs.model.PutMetricFilterRequest;

import java.util.ArrayList;
import java.util.List;

public class AwsMetricFilterPublisher implements MetricFilterPublisher {

    private AWSLogsClient client;

    public AwsMetricFilterPublisher() {
        this(new AWSLogsClient());
    }

    public AwsMetricFilterPublisher(AWSLogsClient client) {
        this.client = client;
    }

    @Override
    public void publishMetricFilter(String logGroupName, String fullMetricName, String filterPatternFormat, List<String> metricValues) {

        System.out.println(String.format("Publishing Metric Filter: [%s] [%s]", logGroupName, fullMetricName));

        String[] parts = fullMetricName.split("/");
        String namespace = parts[0];
        String name = parts[1];

        PutMetricFilterRequest request = new PutMetricFilterRequest()
                .withFilterName(name)
                .withLogGroupName(logGroupName)
                .withFilterPattern(String.format(filterPatternFormat, fullMetricName))
                .withMetricTransformations(buildMetricTransformation(namespace, name, metricValues));

        client.putMetricFilter(request);
    }

    private List<MetricTransformation> buildMetricTransformation(String namespace, String name, List<String> metricValues) {
        List<MetricTransformation> transformations = new ArrayList<>();
        for (String metricValue : metricValues) {
            transformations.add(new MetricTransformation()
                    .withMetricNamespace(namespace)
                    .withMetricName(String.format("%s-%s", name, metricValue))
                    .withMetricValue(String.format("$%s", metricValue)));
        }
        return transformations;
    }
}
