package org.himalay.commandline
import java.util.Map

import org.slf4j.Logger

class CLTBaseQuiet extends org.himalay.commandline.CLTBase{

	public CLTBaseQuiet(Map confData = null)
	{
		super(confData)
	}

	public Object initAutoConf(boolean quiet) {
		return super.initAutoConf(quiet);
	}

	public Map<String, Object> addConf(Map<String, Object> conf) {
		return super.addConf(conf);
	}

	public void info(String mesuperage) {
		super.info(mesuperage);
	}

	public Map<String, Object> getConf() {
		return super.getConf();
	}

	public void debug(String mesuperage) {
		super.debug(mesuperage);
	}

	public void warn(String mesuperage) {
		super.warn(mesuperage);
	}

	public void trace(String mesuperage) {
		super.trace(mesuperage);
	}

	public Logger getLogger() {
		return super.getLogger();
	}

	public int hashCode() {
		return super.hashCode();
	}

	public Logger setLogger(Logger logger) {
		return super.setLogger(logger);
	}

	public Logger searchLogger() {
		return super.searchLogger();
	}

	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public Object getCredentials(String server, String credentialsFile) {
		return super.getCredentials(server, credentialsFile);
	}

	public Object getCredentials(String server) {
		return super.getCredentials(server);
	}

	public String toString() {
		return super.toString();
	}

	public String makeTemplate(String templateText, Object bindings) {
		return super.makeTemplate(templateText, bindings);
	}
	public void info(String message, Throwable throwable)
	{
		super.info(message, throwable)
	}

	public void debug(String message, Throwable throwable)
	{
		super.debug(message, throwable)
	}

	public void error(String message, Throwable throwable)
	{
		super.error(message, throwable)
	}
	
	public void warn(String message, Throwable throwable)
	{
		super.warn(message, throwable)
	}
	
	public void trace(String message, Throwable throwable)
	{
		super.trace(message, throwable)
	}

	@Override
	public void error(String message) {
		super.error(message)
	}

	@Override
	public Object initConf(String confFilePath, boolean quiet) {
		return super.initConf(confFilePath,quiet);
	}

	@Override
	public Map<String, Object> setConf(Map<String, Object> newConf) {
		return super.setConf(newConf)
	}
}
