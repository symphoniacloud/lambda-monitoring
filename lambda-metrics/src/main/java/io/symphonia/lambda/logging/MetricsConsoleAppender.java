package io.symphonia.lambda.logging;

import static io.symphonia.lambda.logging.DefaultConsoleAppender.NO_REQUEST_ID;

public class MetricsConsoleAppender extends LambdaConsoleAppender {

    @SuppressWarnings("WeakerAccess")
    public static String DEFAULT_METRICS_PATTERN =
            "[%d{yyyy-MM-dd HH:mm:ss.SSS}] %X{AWSRequestId:-" + NO_REQUEST_ID +
                    "} METRIC %logger{5} %replace(%msg){'[,= ]+', ' '}%n";

    public MetricsConsoleAppender() {
        super(DEFAULT_METRICS_PATTERN, new MetricPassFilter());
    }

}
