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
	/**
	 * Short option
	 * @return the value
	 */
	String shortOpt      () default "_";
	/**
	 * long options
	 * @return the value
	 */
	String longOpt       () default "";
	/**
	 * name
	 * @return the value
	 */
	String argName       () default "";
	/**
	 * description
	 * @return the value
	 */
	String description   () default "";
	/**
	 * required
	 * @return the value
	 */
	boolean required     () default false;
	/**
	 * optional
	 * @return the value
	 */
	boolean optional     () default true;
	/**
	 * Number of options. Set 0 for boolean options
	 * @return the value
	 */
	int numberOfOptions  () default 1;
	/**
	 * What to do about existence of file.
	 * 0 : Don't care.
	 * 1 : Error out if file does not exist
	 * 2 : Create the file if it does not exist
	 * @return The value
	 */
	int fileExistence    () default 0;
	/**
	 * To indicate that file is actually a folder
	 * @return The value
	 */
	boolean isFolder     () default false;
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
