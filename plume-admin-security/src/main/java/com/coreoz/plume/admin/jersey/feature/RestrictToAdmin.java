package com.coreoz.plume.admin.jersey.feature;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Restrict a resource (JAX-RS Java class) to users having the required permission
 */
@Documented
@Retention (RUNTIME)
@Target({TYPE, METHOD})
public @interface RestrictToAdmin {

	/**
	 * Returns the permission value a user must have to access the resource
	 */
	String value();

}
