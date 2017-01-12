package io.symphonia.lambda.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultMetricsLogbackConfigurator extends ContextAwareBase implements Configurator {

    @Override
    public void configure(LoggerContext loggerContext) {
        addInfo("Setting up default configuration.");

        NonMetricsConsoleAppender nonMetricsConsoleAppender = new NonMetricsConsoleAppender();
        nonMetricsConsoleAppender.setContext(loggerContext);
        nonMetricsConsoleAppender.setName("CONSOLE");
        nonMetricsConsoleAppender.start();

        MetricsConsoleAppender metricsConsoleAppender = new MetricsConsoleAppender();
        metricsConsoleAppender.setContext(loggerContext);
        metricsConsoleAppender.setName("METRICS");
        metricsConsoleAppender.start();

        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(nonMetricsConsoleAppender);
        rootLogger.addAppender(metricsConsoleAppender);
    }

}
