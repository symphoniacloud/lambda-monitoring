package io.symphonia.lambda.annotations;

public @interface CloudwatchLogGroup {
    String value() default "";
}
