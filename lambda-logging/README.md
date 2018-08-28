# lambda-logging

Better logging for Java / JVM [AWS Lambdas](https://aws.amazon.com/lambda/).

`lambda-logging` brings the flexibility of SLF4J and performance of Logback to AWS Lambdas, while retaining the ability
to use the `%X{AWSRequestId}` pattern in log output.

Thanks to SLF4J, it allows Lambda projects to easily replace the myriad of other logging frameworks used throughout
the Java ecosystem.

`lambda-logging` provides a sensible default logging configuration for Logback, whilst still allowing for custom
configuration. It's also compatible with popular logging libraries for other JVM langauges, like `tools.logging` for Clojure and `log4s` for Scala.

## Quick Start

1. **Add the `io.symphonia/lambda-logging` dependency to your project**

   For Maven projects:
   ```xml
   <dependency>
     <groupId>io.symphonia</groupId>
     <artifactId>lambda-logging</artifactId>
     <version>1.0.2</version>
   </dependency>
   ```
   
   The vanilla, no-classifier version of the library includes a default Logback configuration.
   
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
[2017-01-13 21:14:28.401] db0ac08a-dc2e-11e6-bb7c-0117e0d798b2 INFO i.s.Lambda - foo
```

## FAQ

#### How can I change what the output looks like?

1. (optional) Use the `no-config` classifier in the above dependency specification. This will omit the default Logback
configuration:

   ```xml
   <dependency>
     <groupId>io.symphonia</groupId>
     <artifactId>lambda-logging</artifactId>
     <version>1.0.2</version>
     <classifier>no-config</classifier>
   </dependency>
   ```

2. Configure Logback using one of the methods listed here: http://logback.qos.ch/manual/configuration.html. Many folks
choose to put a `logback.xml` file in the project's `src/main/resources` directory.

   For reference, the XML version of the default `lambda-logging` configuration looks like this:

    ```xml
    <configuration>

        <appender name="STDOUT" class="io.symphonia.lambda.logging.DefaultConsoleAppender">
            <encoder>
                <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] %X{AWSRequestId:-" + NO_REQUEST_ID + "} %.-6level %logger{5} - %msg \r%replace(%ex){'\n','\r'}%nopex</pattern>
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
