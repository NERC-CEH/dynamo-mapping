package uk.ac.ceh.dynamo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The following annotation represents that this object is capable of providing
 * map requests and modifying them.
 * 
 * @see Provides
 * @author Christopher Johnson
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Provider {}