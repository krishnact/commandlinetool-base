package org.himalay.commandline

import java.util.Map


import org.slf4j.Logger

import groovy.json.JsonSlurper

trait AutoConfig extends AutoLogger{

	volatile private def config = null;

	public init(boolean quiet) {
		Util util = new Util();
		String className = this.class.canonicalName
		String confFolderName = System.getProperty("_CONF_FOLDER");
		if ( confFolderName == null) {
			confFolderName = System.getenv("_CONF_FOLDER"); 
			if (confFolderName == null){
				confFolderName ="./conf"
			}
		}
		String confFile = "./${confFolderName}/conf.${className}.json"
		initConf(confFile)
		String os = System.getProperties()["os.name"].split(/[\s]+/)[0];


		def platformConfig = util.getJsonConf("./${confFolderName}/conf.${className}."+os.toLowerCase()+".json", quiet)
		synchronized (this) {
		platformConfig.each{
			this.config[it.key] = it.value
		}
	}
	}


	public initConf(String confFilePath){
		synchronized (this) {
			this.config = util.getJsonConf(confFile, quiet);
		}
	}

	Map<String, Object> addConf(Map<String, Object> conf) {
		synchronized (this) {
		conf.each{
			if (! it.key.toString().startsWith("__")){
				this.config[it.key] = it.value
			}
		}
	}
	}

	Map<String, Object> getConf() {
		if (this.config == null) {
			init(true)		
		}
		return this.config
	}

	Map<String, Object> setConf(Map<String, Object> newConf) {
		synchronized (this) {
			this.conf = newConf
		}
		return this.config
	}
}
