package org.himalay.commandline

import java.util.Map


import org.slf4j.Logger

import groovy.json.JsonSlurper

trait AutoConfig extends AutoLogger{

	volatile private def config = null;
	public String confFolderName = System.getProperty("_CONF_FOLDER");
	String confFile   = null;
	String osConfFile = null;

	public String confFile(){
		String className = this.class.canonicalName
		if (confFile == null){
			confFile = "./${confFolderName}/conf.${className}.json"
		}
		return confFile;
	}
	
	public String osConfFile(){
		String className = this.class.canonicalName
		String os = System.getProperties()["os.name"].split(/[\s]+/)[0];
		
		if ( osConfFile == null){
			osConfFile = "./${confFolderName}/conf.${className}."+os.toLowerCase()+".json"
		}
		return osConfFile;
	}

	
	public initAutoConf(boolean quiet) {
		Util util = new Util();
		String className = this.class.canonicalName

		if ( confFolderName == null) {
			confFolderName = System.getenv("_CONF_FOLDER"); 
			if (confFolderName == null){
				confFolderName ="./conf"
			}
		}

		//confFile = "./${confFolderName}/conf.${className}.json"
		initConf(confFile(), quiet)
		//String os = System.getProperties()["os.name"].split(/[\s]+/)[0];


		def platformConfig = util.getJsonConf(osConfFile(), quiet)
		synchronized (this) {
		platformConfig.each{
			this.config[it.key] = it.value
		}
	}
		
		configRead();
	}

	abstract public void configRead();

	public initConf(String confFilePath, boolean quiet){
		synchronized (this) {
			Util util = new Util();
			if (this.config  == null){
				this.config = [:]
			}
			def cc = util.getJsonConf(confFilePath, quiet);
			cc.each{
				this.config[it.key] = it.value
			}
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
		return this.config
	}

	Map<String, Object> setConf(Map<String, Object> newConf) {
		synchronized (this) {
			this.conf = newConf
		}
		return this.config
	}
}
