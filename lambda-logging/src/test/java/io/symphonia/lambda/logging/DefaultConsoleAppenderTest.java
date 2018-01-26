package io.symphonia.lambda.logging;

import com.amazonaws.services.lambda.runtime.LambdaRuntimeInternal;
import org.apache.log4j.MDC;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DefaultConsoleAppenderTest {

    @After
    public void after() {
        MDC.clear();
    }

    @Test
    public void testLambdaRuntimeInternal() throws Exception {
        // Force SLF4J to instantiate our ConsoleAppender
        Logger logger = LoggerFactory.getLogger("TEST-LOGGER");
        assertTrue(LambdaRuntimeInternal.getUseLog4jAppender());
        // The internal Lambda runtime dynamically loads this class to enable MDC - this test makes sure it's there.
        assertNotNull(Thread.currentThread().getContextClassLoader().loadClass("org.apache.log4j.MDC"));
    }

    @Test
    public void testMDC() {
        PrintStream original = System.out;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream, true));

            MDC.put("AWSRequestId", "AWS-REQUEST-ID");
            Logger logger = LoggerFactory.getLogger("TEST-LOGGER");

            logger.info("TEST-MESSAGE");
            assertThat(outputStream.toString(), matchesPattern("^\\[[0-9\\-:\\. ]{23}\\] AWS-REQUEST-ID INFO TEST-LOGGER - TEST-MESSAGE \\r$"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testMDCDefault() {
        PrintStream original = System.out;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream, true));

            Logger logger = LoggerFactory.getLogger("TEST-LOGGER");
            logger.info("TEST-MESSAGE");

            assertThat(outputStream.toString(), matchesPattern("^\\[[0-9\\-:\\. ]{23}\\] NO-REQUEST-ID INFO TEST-LOGGER - TEST-MESSAGE \\r$"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testLogThrowable() {
        PrintStream original = System.out;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream, true));

            Logger logger = LoggerFactory.getLogger("TEST-LOGGER");
            logger.info("TEST-MESSAGE", new Exception("EXCEPTION"));

            assertThat(outputStream.toString(), matchesPattern("(?s)^\\[[0-9\\-:\\. ]{23}\\] NO-REQUEST-ID INFO TEST-LOGGER - TEST-MESSAGE \\rjava\\.lang\\.Exception: EXCEPTION\\r\\tat.*$"));
        } finally {
            System.setOut(original);
        }
    }

}
