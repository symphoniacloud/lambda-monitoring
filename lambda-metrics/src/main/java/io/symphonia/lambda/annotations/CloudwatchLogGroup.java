package io.symphonia.lambda.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CloudwatchLogGroup {
    String value() default "";
}
