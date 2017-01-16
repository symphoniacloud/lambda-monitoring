package io.symphonia.lambda.logging;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.Filter;
import com.amazonaws.services.lambda.runtime.LambdaRuntimeInternal;

public class LambdaConsoleAppender extends ConsoleAppender<ILoggingEvent> {

    public LambdaConsoleAppender() {
        super();
        LambdaRuntimeInternal.setUseLog4jAppender(true);
    }

    public LambdaConsoleAppender(String pattern) {
        this();
        addPattern(pattern);
    }

    public LambdaConsoleAppender(String pattern, Filter<ILoggingEvent> filter) {
        this();
        addPattern(pattern);
        startAndAddFilter(filter);
    }

    private void addPattern(String pattern) {
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern(pattern);
        encoder = patternLayoutEncoder;
    }

    private void startAndAddFilter(Filter<ILoggingEvent> filter) {
        if (filter != null) {
            if (!filter.isStarted()) {
                filter.start();
            }
            addFilter(filter);
        }
    }

    @Override
    public void start() {
        if (encoder != null && !encoder.isStarted()) {
            encoder.setContext(context);
            encoder.start();
        }
        super.start();
    }

}
