package org.himalay.commandline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author krishna
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Option {
	String shortOpt     () default "_";
	String longOpt      () default "";
	String argName      () default "";
	String description  () default "";
	boolean required    () default false;
	boolean optional    () default true;
	int numberOfOptions () default 1;
	/**
	 * The option must match this regex.
	 * @return value
	 */
	String regex        () default "(.)*";
	/**
	 * if this is true and option value starts with '=', a file with the same name will be read and contents be assigned to this option
	 * If the options is a List type then the content of the file will be split by new line operator and resulting array will be assigned to the option.
	 * @return value
	 */
	boolean extend      () default false;
}
