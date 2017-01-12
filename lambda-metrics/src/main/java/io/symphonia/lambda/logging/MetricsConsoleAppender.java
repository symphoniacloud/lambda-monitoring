package io.symphonia.lambda.logging;

public class MetricsConsoleAppender extends LambdaConsoleAppender {

    @SuppressWarnings("WeakerAccess")
    public static String DEFAULT_METRICS_PATTERN =
            "[%d{yyyy-MM-dd HH:mm:ss.SSS}] METRIC %logger{5} %replace(%msg){'[,= ]+', ' '}%n";

    public MetricsConsoleAppender() {
        super(DEFAULT_METRICS_PATTERN, new MetricPassFilter());
    }

}
