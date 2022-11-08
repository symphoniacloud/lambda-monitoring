package io.symphonia.lambda.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultLogbackConfigurator extends ContextAwareBase implements Configurator {

    @Override
    public ExecutionStatus configure(LoggerContext loggerContext) {
        addInfo("Setting up default configuration.");

        DefaultConsoleAppender consoleAppender = new DefaultConsoleAppender();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");
        consoleAppender.start();

        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);

        return ExecutionStatus.NEUTRAL;
    }

}
