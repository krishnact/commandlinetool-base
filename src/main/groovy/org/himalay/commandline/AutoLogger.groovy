package org.himalay.commandline

import java.lang.management.ManagementFactory
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Map

import javax.management.MBeanServer
import javax.management.ObjectName

import org.codehaus.groovy.runtime.callsite.GetEffectivePogoFieldSite

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory;
/**
 * set environment __LOGLEVEL= INFO, DEBUG, TRACE etc to control initial log level.  
 * @author krishna
 *
 */
@CompileStatic
trait AutoLogger {
	public Logger thisLogger = null;
	private void initLogger() {
		Class c = Class.forName("org.himalay.commandline._LOGGER_HELPER")
		synchronized (this) {
			if ( searchLogger() != null ) return;

			thisLogger = LoggerFactory.getLogger('org.himalay.commandline.Autologger');
		}
	}

	public void info(String message) {
		getLogger().info(message)
	}

	public void debug(String message) {
		getLogger().debug(message)
	}

	public void error(String message) {
		getLogger().error(message)
	}

	public void warn(String message) {
		getLogger().warn(message)
	}

	public void trace(String message) {
		getLogger().trace(message)
	}

	public void info(String message, Throwable throwable) {
		getLogger().info(message, throwable)
	}

	public void debug(String message, Throwable throwable) {
		getLogger().debug(message, throwable)
	}

	public void error(String message, Throwable throwable) {
		getLogger().error(message, throwable)
	}

	public void warn(String message, Throwable throwable) {
		getLogger().warn(message, throwable)
	}

	public void trace(String message, Throwable throwable) {
		getLogger().trace(message, throwable)
	}

	public Logger getLogger() {
		if ( this.thisLogger == null) {
			initLogger()
		}
		return thisLogger;
	}

	public Logger setLogger(Logger logger) {
		thisLogger = logger;
	}

	public Logger searchLogger(){
		// If a static logger has been declared then use that.
		try{
			Field mem = this.class.declaredFields.find{
				boolean correctType = it.type == Logger.class ;
				int mod  =  it.modifiers

				boolean correctAccess = ((Modifier.STATIC & it.modifiers ) !=0 )
				return (correctType && correctAccess)
			}
			if ( mem != null)
			{
				String name = mem.name
				String properCaseName = name.substring(0,1).toUpperCase() + name.substring(1)
				def memVal = null
				Class thisClazz = this.class
				Logger ll = null;
				if ((Modifier.PUBLIC & mem.modifiers ) !=0 ) // If public
				{
					Field fld = thisClazz.declaredFields.find{it.name == name}
					ll = (Logger) fld.get(this)
				}else{
					Method method = thisClazz.declaredMethods.find{Method it -> it.name == "get${properCaseName}"}
					ll     = (Logger)method.invoke(this)
				}
				this.setLogger(ll)
				//thisLogger = memVal
			}
		}catch (Exception ex)
		{
			thisLogger.info("Unable to set logger");
		}
		return thisLogger;
	}
}


class LOGGER_CONFIG_LOADER{
	private static Logger _LOGGER = null;
	static String loggerConfigFilePath = null;
			
	static void LOADER() {
		File cf = null
//		String log4jProperties = System.getProperty("log4j2.configurationFile");
//		if ( log4jProperties == null) {
//			log4jProperties = System.getenv("log4j2.configurationFile");
//			if ( log4jProperties == null ){
//				log4jProperties = System.getenv("_LOG4J_PROPERTIES");
//			}
//			if (log4jProperties == null){
//				log4jProperties ="${_CONF_FOLDER.getConfFolderName()}/log4j2.configurationFile"
//			}
//			System.setProperty("log4j2.configurationFile",log4jProperties);
//		}
//		cf = new File(log4jProperties)
				if (_LOGGER == null){
					_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
				}
//		if (!cf.exists()) {
//			_LOGGER.warn("Logger configuration file ${cf.absolutePath} does not exit.")
//		}

	}
}

public class _LOGGER_HELPER{
	
	
	static{
		LOGGER_CONFIG_LOADER.LOADER();
	}
    public static void reloadLogConfiguration() {
		LOGGER_CONFIG_LOADER.LOADER();
	}
}


//public class _LOGGER_HELPER_{
//	//private static Logger _LOGGER = LoggerFactory.getLogger(AutoLogger.class);
//}

class _CONF_FOLDER{
	
	private static String findConfFolder() {
		String confFolderName = System.getProperty("_CONF_FOLDER");
		if (confFolderName ==null) {
			confFolderName = "./conf"
			File confFolder = new File(confFolderName);
			if (!confFolder.exists()) {
				confFolderName = confFolderName = "./config"
			}
		}
		return confFolderName;
	}
	
	private static String confFolderName = findConfFolder();
	
	public static String getConfFolderName () {
		return confFolderName;
	}
}
