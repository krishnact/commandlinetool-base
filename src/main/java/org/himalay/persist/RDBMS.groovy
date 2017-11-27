package org.himalay.persist

import java.io.File
import java.sql.Connection
import java.sql.DriverManager;
import java.sql.ResultSetMetaData

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

	RDBMS(String url, String user, String password, String driver) {
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
	
	/**
	 * 
	 * @param table
	 * @param columns Names of coulmns separated by comma 
	 * @return
	 */
	RDBMS makeNumeric(String table, String columns)
	{
		columns.split(",").each{String column->
			String sqlStr = "alter table ${table} alter column ${column} numeric".toString()
			getSql().execute(sqlStr);
		}
		return this;
	}
	
	public static RDBMS forEachRow(String hibernateConfigFileName , String sql, Closure itemAndIndex ){
		File hibFile = new File(hibernateConfigFileName)
		RDBMS rdbms = new RDBMS(hibFile)
		rdbms.forEachRow(sql, itemAndIndex);
		return RDBMS
	}


	public void eachRow(String sql, Closure itemAndIndex ){
		LOGGER.info ("Executing ${sql}")
		groovy.sql.Sql sqlTmp = getSql();
		int idx = 0;
		try{
			sqlTmp.eachRow(sql) {
				try{
					itemAndIndex(it,idx)
				}catch(Exception ex) {
					LOGGER.error ex.toString()
				}
				idx++
			}
		}catch(Exception ex) {
			LOGGER.error ex.toString()
		}
		//sqlTmp?.close();
	}

	public void forEachRow(String sql, Closure itemAndIndex ){
		LOGGER.info ("Executing ${sql}")
		groovy.sql.Sql sqlTmp = getSql();
		int idx = 0;
		try{
		sqlTmp.rows(sql).each {
			try{
				itemAndIndex(it,idx)
				}catch(Exception ex) {
				LOGGER.error ex.toString()
			}
			idx++
		}
		}catch(Exception ex) {
			LOGGER.error ex.toString()
		}
		//sqlTmp?.close();
	}

	public static RDBMS h2Mem(String name) {
		return new RDBMS("jdbc:h2:mem:${name}",'sa','',"org.h2.Driver");
	}

	/**
	 * Imports a CSV file. It works only with a H2 database. The first row must be column names
	 * @param csvFile
	 * @param tableName
	 * @return the RDBMS object
	 */
	public RDBMS importCSV(File csvFile, String tableName) {
		Sql sql = getSql();
		String sqlStr = "CREATE TABLE if not exists ${tableName} AS SELECT * FROM CSVREAD('${csvFile.absolutePath}');"
		sql.execute(sqlStr);
		return this;
	}

	/**
	 * 
	 * @param ResultSet
	 * @param Writer
	 * @param tableId the table id
	 */
	public static int toHTML(java.sql.ResultSet rs, java.io.Writer out, String tableId)
	throws Exception {
		int rowCount = 0;
		
		out.println("<TABLE id='${tableId}' class='${tableId}_class'>");
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		// table header
		out.println("<TR>");
		for (int i = 0; i < columnCount; i++) {
			out.println("<TH>" + rsmd.getColumnLabel(i + 1) + "</TH>");
		}
		out.println("</TR>");
		// the data
		while (rs.next()) {
			rowCount++;
			out.println("<TR>");
			for (int i = 0; i < columnCount; i++) {
				out.println("<TD>" + rs.getString(i + 1) + "</TD>");
			}
			out.println("</TR>");
		}
		out.println("</TABLE>");
		return rowCount;
	}

}
