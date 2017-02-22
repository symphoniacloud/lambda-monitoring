# lambda-monitoring

A collection of logging and metrics libraries for Java / JVM [AWS Lambdas](https://aws.amazon.com/lambda/).

Background reading:
 - [A Love Letter to Lambda Logging](https://medium.com/the-symphonium/a-love-letter-to-lambda-logging-974b0eb49273#.b7egnww56)
 - [The Danger in the Details - Scalable Cloudwatch Metrics for AWS Lambda](https://medium.com/the-symphonium/the-danger-in-the-details-scalable-cloudwatch-metrics-for-aws-lambda-b6c2910cb09c#.m3cc61ih1)

## [lambda-logging](lambda-logging/)

Modern logging for Lambdas, using SLF4J + Logback.

## [lambda-metrics](lambda-metrics/)

Loggable Codahale metrics for Lambdas.

## [lambda-metrics-maven-plugin](lambda-metrics-maven-plugin/)

Annotation-driven, automated Cloudwatch Metric Filters.
