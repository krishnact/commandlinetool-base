package org.himalay.commandline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author krishna
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Configure {
	String key              () default "";
	String defaultVal       () default "";
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

}
