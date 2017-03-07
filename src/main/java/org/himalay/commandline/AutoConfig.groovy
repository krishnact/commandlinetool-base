package org.himalay.commandline

import java.util.Map


import org.slf4j.Logger

import groovy.json.JsonSlurper

trait AutoConfig extends AutoLogger{

	private def config = null;

	public init(boolean quiet)
	{
		Util util = new Util();
		String className = this.class.canonicalName
		String confFolderName = System.getProperty("_CONF_FOLDER");
		if ( confFolderName == null)
		{
			confFolderName = System.getenv("_CONF_FOLDER"); 
			if (confFolderName == null){
				confFolderName ="./conf"
			}
		}
		this.config = util.getJsonConf("./${confFolderName}/conf.${className}.json", quiet);
		String os = System.getProperties()["os.name"].split(/[\s]+/)[0];


		def platformConfig = util.getJsonConf("./${confFolderName}/conf.${className}."+os.toLowerCase()+".json", quiet)
		platformConfig.each{
			this.config[it.key] = it.value
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
		if (this.config == null)
		{
			init(true)		
		}
		return this.config
	}



}
