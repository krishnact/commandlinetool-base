package org.himalay.commandline

import java.lang.management.ManagementFactory
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Map

import javax.management.MBeanServer
import javax.management.ObjectName

import org.codehaus.groovy.runtime.callsite.GetEffectivePogoFieldSite
import org.omg.PortableInterceptor.AdapterNameHelper

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
		synchronized (this) {
			if ( searchLogger() != null ) return;

			thisLogger = LoggerFactory.getLogger(AutoLogger.class);
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
		Class.forName("org.himalay.commandline._LOGGER_HELPER")
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
	static ObjectName beanName = null;
	static void LOAFDER() {
		boolean useLog4j2 = true;
		try{
			Class cls = Class.forName('org.apache.logging.log4j.core.config.ConfigurationSource');
		}catch(Exception ex){
			useLog4j2 = false;
		}

		if (useLog4j2 == true){
			String log4jProperties = System.getProperty("log4j.configurationFile");
			if ( log4jProperties == null) {
				log4jProperties = System.getenv("log4j.configurationFile");
				if ( log4jProperties == null ){
					log4jProperties = System.getenv("_LOG4J_PROPERTIES");
				}
				if (log4jProperties == null){
					log4jProperties ="./${_CONF_FOLDER.getConfFolderName()}/log4j.configurationFile"
				}
			}
			
			Properties props = new Properties();
			def logLevel = System.getenv()["__LOGLEVEL"]
			if (logLevel == null ) logLevel='INFO'
			String log4jText = """
			log4j.rootLogger=${logLevel}, A1
			
			log4j.appender.A1=org.apache.log4j.ConsoleAppender
			log4j.appender.A1.layout=org.apache.log4j.PatternLayout
			# Print the date in ISO 8601 format
			log4j.appender.A1.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
			log4j.logger.org.apache=DEBUG
			"""
			Properties ppp = new Properties()


			try {
				File propsFile = new File(log4jProperties)
				if ( propsFile.size() > 16) // Zero size files can be a problem sometimes.
				{
					Class.forName("org.apache.logging.log4j.core.config.Configurator").initialize(null, propsFile);
					_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
				}else{
					Class cls = Class.forName("org.apache.logging.log4j.core.config.ConfigurationSource");
					def source = cls.newInstance(new FileInputStream(propsFile));
					//source = new org.apache.logging.log4j.core.config.ConfigurationSource();
					Class.forName("org.apache.logging.log4j.core.config.Configurator").initialize(null, source);
					_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
					if ( System.getenv()['__AUTOLOGGER_QUIET'] == null){
						_LOGGER.info("Continuing with default log4j properties")
					}
				}
			} catch (Exception e) {
				Class cls = Class.forName("org.apache.logging.log4j.core.config.ConfigurationSource");
				def source = cls.newInstance(new ByteArrayInputStream(log4jText.bytes));
				Class.forName("org.apache.logging.log4j.core.config.Configurator").initialize(null, source);
				if (_LOGGER == null){
					_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
				}
				_LOGGER.info("While reading config file ${log4jProperties}", e) ;
				_LOGGER.info("Continuing with default log4j properties")
			}
			loggerConfigFilePath = log4jProperties;
		}else{
			String log4jProperties = System.getProperty("_LOG4J_PROPERTIES");
			if ( log4jProperties == null) {
				log4jProperties = System.getenv("_LOG4J_PROPERTIES");
				if (log4jProperties == null){
					log4jProperties ="./${_CONF_FOLDER.getConfFolderName()}/log4j.properties"
				}
			}
			Properties props = new Properties();
			def logLevel = System.getenv()["__LOGLEVEL"]
			if (logLevel == null ) logLevel='INFO'
			String log4jText = """
			log4j.rootLogger=${logLevel}, A1
			
			log4j.appender.A1=org.apache.log4j.ConsoleAppender
			log4j.appender.A1.layout=org.apache.log4j.PatternLayout
			# Print the date in ISO 8601 format
			log4j.appender.A1.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
			log4j.logger.org.apache=DEBUG
			"""
			Properties ppp = new Properties()
			ppp.load(new ByteArrayInputStream(log4jText.bytes))

			Class properyCOnfigureator = null;
			try{
				properyCOnfigureator = Class.forName("org.apache.log4j.PropertyConfigurator");
				//properyCOnfigureator.configure(ppp)
			}catch(Exception ex){
				if(logLevel == "DEBUG"){
					ex.printStackTrace();
				}
			}
			if (properyCOnfigureator != null){
				try {
					
					File propsFile = new File(log4jProperties)
					if ( propsFile.size() > 16) // Zero size files can be a problem sometimes.
					{
						props.load(new FileInputStream(propsFile));
						Class.forName('org.apache.log4j.PropertyConfigurator').configure(props);
						_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
					}else{
						if ( System.getenv()['__AUTOLOGGER_QUIET'] == null){
							_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
							_LOGGER.info("Continuing with default log4j properties. Set __AUTOLOGGER_QUIET=1 to suppress this message.")
						}
						properyCOnfigureator.configure(ppp);
					}
				} catch (Exception e) {
					Class.forName('org.apache.log4j.PropertyConfigurator').configure(new ByteArrayInputStream(log4jText.bytes));
					_LOGGER = LoggerFactory.getLogger(AutoLogger.class);
					if ( System.getenv()['__AUTOLOGGER_QUIET'] == null){
						_LOGGER.info("While reading config file ${log4jProperties}", e) ;
						_LOGGER.info("Continuing with default log4j properties");
					}
				}
				loggerConfigFilePath = log4jProperties;
				if ( beanName == null) {
					//Get the MBean server
					MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
					//register the MBean
					Log4jConfiguratorMXBean mBean = new Log4jConfigurator();
					beanName = new ObjectName("org.himalay.commandlingtool:type=LoggingConfig");
					mbs.registerMBean(mBean, beanName);
				}
			}
		}

	}
}

public class _LOGGER_HELPER{
	
	
	static{
		LOGGER_CONFIG_LOADER.LOAFDER();
	}
    public static void reloadLogConfiguration() {
		LOGGER_CONFIG_LOADER.LOAFDER();
	}
}


public class _LOGGER_HELPER_{
	private static Logger _LOGGER = LoggerFactory.getLogger(AutoLogger.class);
}

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
