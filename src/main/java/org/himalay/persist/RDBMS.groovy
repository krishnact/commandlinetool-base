package org.himalay.persist

import java.io.File
import java.sql.Connection
import java.sql.DriverManager;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import groovy.sql.Sql;

class RDBMS {
	static Logger LOGGER = LoggerFactory.getLogger(RDBMS.class)
	groovy.sql.Sql sql
	def db = [:]

	public Sql getSql() {
		
		if ( sql == null){
			Sql.loadDriver(db.driver)
			Connection connection = DriverManager.getConnection(db.url, db.user, db.password);
			//sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
			sql = new Sql(connection);
		}
		return sql;
	}

	RDBMS(String url, String user, String password, String driver, String tableName="CachedItem") {
		db.url         = url       ;
		db.user        = user      ;
		db.password    = password  ;
		db.driver      = driver    ;
	}

	RDBMS() {
		this(new File(System.getProperty("HIBERNATE_CONF")))
	}
	RDBMS(File hibernateCfgXml) {

		XmlSlurper xmlSlurper = new XmlSlurper();
		xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
		xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		def root = xmlSlurper.parse(hibernateCfgXml)

		db.url         = root.depthFirst().find { it.name() == 'property' && it.@name == 'connection.url'}.text()
		db.user        = root.depthFirst().find { it.name() == 'property' && it.@name == 'connection.username'}.text()
		db.password    = root.depthFirst().find { it.name() == 'property' && it.@name == 'connection.password'}.text()
		db.driver      = root.depthFirst().find { it.name() == 'property' && it.@name == 'connection.driver_class'}.text()
	}
	
	public static void forEachRow(String hibernateConfigFileName , String sql, Closure itemAndIndex ){
		File hibFile = new File(hibernateConfigFileName)
		RDBMS rdbms = new RDBMS(hibFile)
		LOGGER.info ("Executing ${sql}")
		groovy.sql.Sql sqlTmp = rdbms.getSql();
		int idx = 0;
		try{
		sqlTmp.rows(sql).each {
			try{
				itemAndIndex(it,idx)
			}catch(Exception ex)
			{
				LOGGER.error ex.toString()
			}
			idx++
		}
		}catch(Exception ex)
		{
			LOGGER.error ex.toString()
		}
		sqlTmp?.close();
	}
}
