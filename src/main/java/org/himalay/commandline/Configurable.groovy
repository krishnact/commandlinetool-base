package org.himalay.commandline;

import java.util.Map

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.log4j.PropertyConfigurator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper

class Configurable {
	private static Logger LOGGER = LoggerFactory.getLogger(Configurable.class);
	public Logger thisLogger = LOGGER;
	def config = null;

	static{
		String log4jProperties = System.getProperty("_LOG4J_PROPERTIES");
		if ( log4jProperties == null)
		{
			log4jProperties ="./conf/log4j.properties"
		}

		Properties props = new Properties();
				String log4jText = '''
log4j.rootLogger=INFO, A1

log4j.appender.A1=org.apache.log4j.ConsoleAppender
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
# Print the date in ISO 8601 format
log4j.appender.A1.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
log4j.logger.org.apache=WARN
'''
		try {
			File propsFile = new File(log4jProperties)
			if ( propsFile.exists() && propsFile.size() > 16) // Zero size files can be a problem sometimes.
			{
				props.load(new FileInputStream(propsFile));
				PropertyConfigurator.configure(props);
			}else{
				PropertyConfigurator.configure(new ByteArrayInputStream(log4jText.bytes))
				LOGGER.info("Continuing with default log4j properties. To supress this message, please create a file: ${log4jProperties}, which is at leaset 16 charcaters long.")
			}
		} catch (Exception e) {
			PropertyConfigurator.configure(new ByteArrayInputStream(log4jText.bytes))
			LOGGER.info("While reading config file ${log4jProperties}", e) ;//ExceptionUtils.getStackFrames(e).join("\n"));
			LOGGER.info("Continuing with default log4j properties.")
		}
		
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			LOGGER.info(ExceptionUtils.getFullStackTrace(e));
//		}
	}
	public Configurable(boolean quiet)
	{
		String className = this.class.canonicalName
		String confFolderName = System.getProperty("_CONF_FOLDER");
		if ( confFolderName == null)
		{
			confFolderName ="./conf"
		}
		this.config = getJsonConf("./${confFolderName}/conf.${className}.json", quiet);
		String os = System.getProperties()["os.name"].split(/[\s]+/)[0];
		
		
		def platformConfig = getJsonConf("./${confFolderName}/conf.${className}."+os.toLowerCase()+".json", quiet)
		platformConfig.each{
			this.config[it.key] = it.value
		}
	}
	public Configurable()
	{
		this(true)
	}
	
	public static def getJsonConf(String confFilePath, boolean quiet)
	{
		JsonSlurper js = new JsonSlurper();
		File confFile = new File(confFilePath)
		if ( confFile.exists()){
			if ( !quiet){
				LOGGER.debug("Parsing file  with the path = ${confFile.absolutePath}")
			}else{
				LOGGER.trace("Parsing file  with the path = ${confFile.absolutePath}")
			}
			return js.parse(confFile);//))
		}else{
			if (!quiet){
				LOGGER.info("There is no conf file with the path = ${confFile.absolutePath}")
			}else{
				LOGGER.debug("There is no conf file with the path = ${confFile.absolutePath}")
			}
			return [:]
		}
		
	}
	Map<String, Object> addConf(Map<String, Object> conf)
	{
		conf.each{
			if (! it.key.toString().startsWith("__")){
				this.config[it.key] = it.value
			}
		}
	}
	
	Map<String, Object> getConf()
	{
		return this.config
	}
	
	public void info(String message)
	{
		thisLogger.info(message)
	}
	
	public void debug(String message)
	{
		thisLogger.debug(message)
	}

	public void warn(String message)
	{
		thisLogger.warn(message)
	}

	public void trace(String message)
	{
		thisLogger.trace(message)
	}

}
