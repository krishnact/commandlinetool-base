package org.himalay.commandline

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Map


import org.codehaus.groovy.tools.shell.commands.AliasTargetProxyCommand

import org.codehaus.groovy.runtime.callsite.GetEffectivePogoFieldSite
import org.omg.PortableInterceptor.AdapterNameHelper
import org.apache.log4j.PropertyConfigurator
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory;
@CompileStatic
trait AutoLogger {
	public Logger thisLogger = null;
	private void initLogger()
	{
		synchronized (this) {
			if ( searchLogger() != null ) return;
			
			thisLogger = LoggerFactory.getLogger(AutoLogger.class);
		}
	}

	public void info(String message)
	{
		getLogger().info(message)
	}

	public void debug(String message)
	{
		getLogger().debug(message)
	}

	public void error(String message)
	{
		getLogger().error(message)
	}
	
	public void warn(String message)
	{
		getLogger().warn(message)
	}
	
	public void trace(String message)
	{
		getLogger().trace(message)
	}
	
	public void info(String message, Throwable throwable)
	{
		getLogger().info(message, throwable)
	}

	public void debug(String message, Throwable throwable)
	{
		getLogger().debug(message, throwable)
	}

	public void error(String message, Throwable throwable)
	{
		getLogger().error(message, throwable)
	}
	
	public void warn(String message, Throwable throwable)
	{
		getLogger().warn(message, throwable)
	}
	
	public void trace(String message, Throwable throwable)
	{
		getLogger().trace(message, throwable)
	}
	
	public Logger getLogger()
	{
		Class.forName("org.himalay.commandline._LOGGER_HELPER")
		if ( this.thisLogger == null)
		{
				initLogger()
		}
		return thisLogger;
	}
	
	public Logger setLogger(Logger logger)
	{
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
				if ((Modifier.PUBLIC & mem.modifiers ) !=0 ) // If public
				{
					Field fld = thisClazz.declaredFields.find{it.name == name}
					Method set = thisClazz.declaredMethods.find{Method it -> it.name == "setLogger"}
					Logger ll = (Logger) fld.get(this)
					this.setLogger(ll)
				}else{
					Method method = thisClazz.declaredMethods.find{Method it -> it.name == "get${properCaseName}"}
					Method set = thisClazz.declaredMethods.find{Method it -> it.name == "setLogger"}
					//thisLogger = this."get${properCaseName}"()
					Logger ll     = (Logger)method.invoke(this)
					this.setLogger(ll)
				}

				//thisLogger = memVal
			}
		}catch (Exception ex)
		{
			ex.printStackTrace()
		}
		return thisLogger;
	}
}

public class _LOGGER_HELPER{
	private static Logger _LOGGER = LoggerFactory.getLogger(AutoLogger.class);
	static{
		String log4jProperties = System.getProperty("_LOG4J_PROPERTIES");
		if ( log4jProperties == null) {
			log4jProperties = System.getenv("_LOG4J_PROPERTIES");
			if (log4jProperties == null){
				log4jProperties ="./conf/log4j.properties"
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
			log4j.logger.org.apache=WARN
			"""
		PropertyConfigurator.configure(new ByteArrayInputStream(log4jText.bytes))
		
		try {
			File propsFile = new File(log4jProperties)
			if ( propsFile.size() > 16) // Zero size files can be a problem sometimes.
			{
				props.load(new FileInputStream(propsFile));
				PropertyConfigurator.configure(props);
			}else{
				if ( System.getenv()['__QUIET'] == null){
					_LOGGER.info("Continuing with default log4j properties")
				}
			}
		} catch (Exception e) {
			PropertyConfigurator.configure(new ByteArrayInputStream(log4jText.bytes))
			_LOGGER.info("While reading config file ${log4jProperties}", e) ;//ExceptionUtils.getStackFrames(e).join("\n"));
			_LOGGER.info("Continuing with default log4j properties")
		}

	}
}