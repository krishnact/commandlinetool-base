package org.himalay.persist

import java.io.File
import java.sql.Connection
import java.sql.DriverManager;
import java.sql.ResultSetMetaData

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql;

class RDBMS {
	static Logger LOGGER = LoggerFactory.getLogger(RDBMS.class)
	private groovy.sql.Sql sql_
	Connection connection;
	def db = [:]

	public Sql getSql() {
		synchronized (this) {
			if ( sql_ == null){
			Sql.loadDriver(db.driver)
			connection = DriverManager.getConnection(db.url, db.user, db.password);
			//sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
			sql_ = new Sql(connection);
			}
			return sql_;
		}
	}

	public void init_(){
		synchronized (this) {
			this.sql = null;
			this.connection?.close()
		}
	}

	public RDBMS(String url, String user, String password, String driver) {
		db.url         = url       ;
		db.user        = user      ;
		db.password    = password  ;
		db.driver      = driver    ;
	}

	public RDBMS() {
		this(new File(System.getProperty("HIBERNATE_CONF")))
	}
	public RDBMS(File hibernateCfgXml) {

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
	public RDBMS makeNumeric(String table, String columns)
	{
		columns.split(",").each{String column->
			String sqlStr = "alter table ${table} alter column ${column} numeric".toString()
			getSql().execute(sqlStr);
		}
		return this;
	}
	

	/**
	 * 
	 * @param table
	 * @param columns Names of coulmns separated by comma 
	 * @return
	 */
	public RDBMS makeBigint(String table, String columns)
	{
		columns.split(",").each{String column->
			String sqlStr = "alter table ${table} alter column ${column} bigint".toString()
			getSql().execute(sqlStr);
		}
		return this;
	}

	/**
	 *
	 * @param table
	 * @param columns Names of coulmns separated by comma
	 * @return
	 */
	public RDBMS makeType(String table, String columns, String newType)
	{
		columns.split(",").each{String column->
			String sqlStr = "alter table ${table} alter column ${column} ${newType}".toString()
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


	/**
	 * 
	 * @param sql
	 * @param itemAndIndex A closure that takes groovy.sql.GroovyResultSet and int as arguments
	 */
	public void eachRow(String sql, Closure itemAndIndex ){
		LOGGER.debug ("Executing ${sql}")
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
	}

	/**
	 * 
	 * @param sql
	 * @param itemAndIndex A closure that takes groovy.sql.GroovyRowResult and int as arguments
	 */
	public void forEachRow(String sql, Closure itemAndIndex ){
		LOGGER.debug ("Executing ${sql}")
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
	}

	/**
	 * 
	 * @param name
	 * @param startServer If passed true, then database server will listen on 
	 *                    http port 8082 and browser will be launched. The JDBC 
	 *                    URL for connection is printed in log statement
	 * @return
	 */
	public static RDBMS h2Mem(String name, boolean startServer = false) {
		RDBMS retVal = new RDBMS("jdbc:h2:mem:${name}",'sa','',"org.h2.Driver");
		if (startServer){
			Class clz = Class.forName("org.h2.tools.Server");
			
			clz."createTcpServer"();
			clz."main"();
			LOGGER.info "Using ${retVal.db.url}, userName: ${retVal.db.user}"
		}
		return retVal;
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

	public Table toTable(String sql){
		Table table = new Table('rowId','', true);
		this.forEachRow(sql){GroovyRowResult grr, int idx->
			table.table[idx]=grr.collectEntries{
				return [it.key,it.value]
			}
		}
		
		return table
	}
	
	public RDBMS h2AutoColumnsType(String table, int limit){
		def dataTypes = [:].withDefault{'text'};
		this.eachRow("select * from ${table} limit ${limit}"){GroovyResultSet grs, idx->
			(1..grs.getMetaData().columnCount).each{int idxx->
				String value = grs.getString(idxx);
				String colName = $/"${grs.getMetaData().getColumnName(idxx)}"/$;
				if (dataTypes[colName] == 'text') {
					if ( value ==~ /[\-]*[0-9]+/) {
						dataTypes[colName] = 'BIGINT';
					}else if ( value ==~ /[\-]*[0-9]+[.][0-9]*/){
						dataTypes[colName] = 'DOUBLE';
					}
				}else if (dataTypes[colName] == 'BIGINT') {
					if ( value ==~ /[\-]*[0-9]+[.][0-9]*/){
						dataTypes[colName] = 'DOUBLE';
					}
				}
			}
		}
		def columnTypes = [:].withDefault{[]}
		dataTypes.each{key,val->
			columnTypes[val] << key
		}
		columnTypes.remove('text');
		columnTypes.each{key, List val->
			this.makeType(table, val.join(","),key)
		}

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
