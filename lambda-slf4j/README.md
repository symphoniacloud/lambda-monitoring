# lambda-slf4j

Better logging for Java / JVM [AWS Lambdas](https://aws.amazon.com/lambda/).

`lambda-slf4j` brings the flexibility of SLF4J and performance of Logback to AWS Lambdas, while retaining the ability
to use the `%X{AWSRequestId}` pattern in log output.

Thanks to SLF4J, it allows Lambda projects to easily replace the myriad of other logging frameworks used throughout
the Java ecosystem.

Last but not least, it provides a sensible default logging configuration for Logback, whilst still allowing for custom
configuration.

## Quick Start

1. **Add the `io.symphonia/lambda-slf4j` dependency to your project, using the `with-config` classifier**

   For Maven projects:
   ```xml
   <dependency>
     <groupId>io.symphonia</groupId>
     <artifactId>lambda-slf4j</artifactId>
     <version>2.0.0</version>
     <classifier>with-config</classifier>
   </dependency>
   ```
   
   `with-config` just means that this flavor of the library includes a default Logback configuration.
   
1. ***Start logging!***

    ```java
    package io.symphonia;
    
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    public class Lambda {
        private static Logger LOG = LoggerFactory.getLogger(Lambda.class);
    
        public void handler(String input) {
            LOG.info(input);
        }
    }
    ```
    
Given an input string of "foo", the output in Cloudwatch Logs looks like:

```
[2017-01-13 21:14:28.401] INFO i.s.Lambda - foo
```

## FAQ

#### How can I change what the output looks like?

1. Omit the `with-config` classifier from the above dependency specification. The no-classifier flavor of the library
doesn't include any Logback configuration.

2. Configure Logback using one of the methods listed here: http://logback.qos.ch/manual/configuration.html. Many folks
choose to put a `logback.xml` file in the project's `src/main/resources` directory.

   For reference, the XML version of the default `lambda-slf4j` configuration looks like this:

    ```xml
    <configuration>

        <appender name="STDOUT" class="io.symphonia.lambda.logging.DefaultConsoleAppender">
            <encoder>
                <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] %X{AWSRequestId:-NO-REQUEST-ID} %.-6level %logger{5} - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="info">
            <appender-ref ref="STDOUT" />
        </root>
        
    </configuration>
    ```
 
## TODO

If [LambdaRuntime.logger](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/LambdaRuntime.java#L8-L12) 
ever gets more sophisticated than `System.out.println`, the `LambdaConsoleAppender` will need to actually delegate to 
whatever that implementation is.
