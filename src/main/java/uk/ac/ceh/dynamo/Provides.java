package uk.ac.ceh.dynamo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The following annotation represents that this object is capable of providing
 * map requests and modifying them.
 * 
 * @see Provider
 * @author Christopher Johnson
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Provides {
    DynamoMapMethod[] value();
}