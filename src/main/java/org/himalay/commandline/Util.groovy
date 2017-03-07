package org.himalay.commandline

import groovy.json.JsonSlurper

class Util implements AutoLogger {

	
	public def getJsonConf(String confFilePath, boolean quiet)
	{
		JsonSlurper js = new JsonSlurper();
		File confFile = new File(confFilePath)
		if ( confFile.exists()){
			if ( !quiet){
				debug("Parsing file  with the path = ${confFile.absolutePath}")
			}else{
				trace("Parsing file  with the path = ${confFile.absolutePath}")
			}
			return js.parse(confFile);//))
		}else{
			if (!quiet){
				info("There is no conf file with the path = ${confFile.absolutePath}")
			}else{
				debug("There is no conf file with the path = ${confFile.absolutePath}")
			}
			return [:]
		}

	}
}
