package org.himalay.commandline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author krishna
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Arg {
	String shortOpt     () default "_";
	String longOpt      () default "";
	String argName      () default "";
	String description  () default "";
	boolean required    () default false;
	boolean optionalArg () default true;
	int numberOfArgs    () default 1;
	String regex        () default "(.)*";
	boolean extend      () default true;
}
