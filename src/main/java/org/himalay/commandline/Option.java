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
	String shortOpt      () default "_";
	String longOpt       () default "";
	String argName       () default "";
	String description   () default "";
	boolean required     () default false;
	boolean optional     () default true;
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
