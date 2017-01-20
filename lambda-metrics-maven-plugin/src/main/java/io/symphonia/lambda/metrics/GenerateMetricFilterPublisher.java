package io.symphonia.lambda.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class GenerateMetricFilterPublisher implements MetricFilterPublisher {

    private final Log log;

    private final File outputFile;

    public GenerateMetricFilterPublisher(Log log, File outputDirectory) {
        this.log = log;
        assert (outputDirectory.isDirectory());
        this.outputFile = new File(outputDirectory, "metric-filters.json");
    }

    @Override
    public boolean removeMetricFilter(String logGroupName, String fullMetricName, String metricValue) {
        return false;
    }

    @Override
    public boolean publishMetricFilter(MetricFilter metricFilter) {
        return false;
    }

    @Override
    public int removeMetricFilters(List<MetricFilter> metricFilters) {
        return publishMetricFilters(metricFilters);
    }

    @Override
    public boolean removeMetricFilter(MetricFilter metricFilter) {
        return publishMetricFilter(metricFilter);
    }

    @Override
    public int publishMetricFilters(Collection<MetricFilter> metricFilters) {
        log.info("Writing metric filters to output file: " + outputFile.getPath());
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(outputFile, metricFilters);
        } catch (IOException e) {
            log.error("Error writing JSON to output file.", e);
        }
        return 0;
    }

}
