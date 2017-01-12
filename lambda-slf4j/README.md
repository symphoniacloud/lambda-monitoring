# io.symphonia/lambda-slf4j

Better logging for Java / JVM [AWS Lambdas](https://aws.amazon.com/lambda/).

`lambda-slf4j` brings the benefits and flexibility of SLF4J to AWS Lambdas, while retaining the ability to seamlessly include the Lambda's AWS Request ID in log output using the `%{AWSRequestId}` conversion pattern. It allows Lambda projects to use SLF4J to replace the myriad of logging frameworks used by other Java libraries and consolidate logging configuration into a single file.

## Quick Start

1. **Add the `io.symphonia/lambda-slf4j` dependency to your project**

   For Maven projects:
   ```xml
   <dependency>
     <groupId>io.symphonia</groupId>
     <artifactId>lambda-slf4j</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```
   
   For Leiningen projects (e.g. for Clojure Lambdas):
   ```clojure
   [io.symphonia/lambda-slf4j "1.0.0"]
   ```

1. **Add the appropriate [SLF4J bridge libraries](http://www.slf4j.org/legacy.html) to your project, if necessary.**

   Some AWS libraries (like the AWS Java SDK)
   [use `commons-logging`](https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-core/pom.xml#L16-L20). If you have
   such libraries in your project, you'll need to add a dependency on SLF4J's `jcl-over-slf4j` bridge.
   
   Note that the `log4j-over-slf4j` bridge is already included as a dependency of `lambda-slf4j` (this library),
   so if you depend on libraries that require log4j no extra work is required for them.
   
   Maven:
   ```xml
   <dependency>
     <groupId>org.slf4j</groupId>
     <artifactId>jcl-over-slf4j</artifactId>
     <version>1.7.22</version>
   </dependency>
   ```
   
   Leiningen:
   ```clojure
   [org.slf4j/jcl-over-slf4j "1.7.22"]
   ```

1. **Copy this Logback configuration file into `logback.xml` to your project's main resource directory (`src/main/resources`).** 

   ```xml
   <configuration scan="false">
       <appender name="STDOUT" class="io.symphonia.lambda.ConsoleAppender">
           <encoder>
               <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %X{AWSRequestId:-NO-REQUEST-ID} %.-1level %logger{5} - %msg%n</pattern>
           </encoder>
       </appender>
       <root level="debug">
           <appender-ref ref="STDOUT"/>
       </root>
   </configuration>
   ```

## Why replace the default logging library?

The default AWS JVM Lambda logging library is `aws-lambda-java-log4j` . This does a couple of handy things:

1. Invokes the `LambdaRuntimeInternal.setUseLog4jAppender` method with the boolean parameter `true`, which causes the 
[internal EventHandlerLoader](https://github.com/aldrinleal/lambda-java-runtime/blob/99706a1db64f95f8f86d3db96ca8a39297d1b669/lambdainternal/EventHandlerLoader.java#L486-L499) 
to load a nice [MDC](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/MDC.html) that allows access to the 
AWS Request Id in logging patterns.
1. Prints `Throwable` stack traces as a single string with embedded newlines, in cases where the configured Log4J 
layout doesn't print Throwables.

Unfortunately, for an otherwise very modern Serverless platform, it also brings an almost [five year old version](https://mvnrepository.com/artifact/log4j/log4j/1.2.17) of an 
[unmaintained logging package](https://blogs.apache.org/foundation/entry/apache_logging_services_project_announces) 
into your Lambda project as a transitive dependency.

## Why use SLF4J?

[SLF4J](http://www.slf4j.org/) is an attractive alternative to Log4J 1.x, due to its:

1. Compatibility with Log4J features like MDCs.
1. Ability to replace and consolidate all other Java logging frameworks.
1. Superior runtime performance.
1. Smaller dependency footprint.

## TODO

If [LambdaRuntime.logger](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-core/src/main/java/com/amazonaws/services/lambda/runtime/LambdaRuntime.java#L8-L12) 
ever gets more sophisticated than `System.out.println`, the `LambdaConsoleAppender` will need to actually delegate to 
whatever that implementation is.
