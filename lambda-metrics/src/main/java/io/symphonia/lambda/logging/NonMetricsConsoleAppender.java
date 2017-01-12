package io.symphonia.lambda.logging;

import static io.symphonia.lambda.logging.DefaultConsoleAppender.DEFAULT_PATTERN;

public class NonMetricsConsoleAppender extends LambdaConsoleAppender {

    public NonMetricsConsoleAppender() {
        super(DEFAULT_PATTERN, new MetricBlockFilter());
    }

}