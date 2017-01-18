# lambda-metrics-maven-plugin

Annotation-driven, automated Cloudwatch Metric Filters publishing.

## Quick Start

1. **Add the `io.symphonia/lambda-metrics-maven-plugin` plugin to your Maven project**

   ```xml
   <build>
      <plugins>
         <plugin>
            <groupId>io.symphonia</groupId>
            <artifactId>lambda-metrics-maven-plugin</artifactId>
            <version>1.0.0</version>>
         </plugin>
      </plugins>
   </build>
   ```
   
2. **Execute the `publish` goal to publish Metric Filters**
   
   The default plugin execution isn't tied to a lifecycle phase, you must manually invoke it:
   
   ```shell
   $ mvn lambda-metrics:publish -DdryRun=true
   ```
   
   Note that in this example, we've given the `dryRun=true` parameter, which will simulate the actions
   that will be taken, but won't actually publish any Metric Filters to your AWS account.   
   
   The output from that command looks like this:
   
   ```shell
   [INFO] Found [1] metric fields in classpath.
   [INFO] [dry run] Publishing metric filter [fooCounter-count] to log group [/aws/lambda/myLambda]
   [INFO] Published [0] metric filters.
   ```
   
   If the `dryRun=true` parameter was omitted, that last line would indicate how many Metric Filters 
   were actually published to AWS.