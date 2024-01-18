package org.himalay.persist

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import groovy.sql.Sql
/**
 * CREATE TABLE    IF NOT EXISTS CachedItem (_ID IDENTITY , _KEY text, value text(10000000), updatedAt TIMESTAMP, status number );
 * 
 * @author krishna
 *
 */
class Cache extends RDBMS{
	static Logger LOGGER = LoggerFactory.getLogger(Cache.class)
	String tableName
	static String CREATE_TABLE_sql =  "CREATE TABLE IF NOT EXISTS CachedItem (uid IDENTITY , key1 varchar(256), key2 varchar(256), value text(10000000), updatedAt TIMESTAMP, status number );"
	Closure filler= null;
	Closure expired = null
	boolean deleteOnExpired = false; 
	Cache(File hibernateCfgXml) {
		super(hibernateCfgXml)
	}

	
	Cache(String url, String user, String password, String driver, String tableName="CachedItem") {
		super(url, user, password, driver);
		this.tableName = tableName
		init()
	}

	private void init() {
		sql.execute(CREATE_TABLE_sql)
	}

	Cache(String hibernateCfgXml) {
		this(new File(hibernateCfgXml))
		init()
	}

	public CachedItem findOne(String key1, String key2) {
		CachedItem ret;
		def row = getSql().rows("select * from CachedItem where key1=? and key2 =?", key1,  key2 )
		if ( row.size() > 0) {
			ret = new CachedItem(row[0]);
			if (expired != null && expired.call(ret.updatedAt, ret) ){
				ret = null;
				if (deleteOnExpired == true){
					getSql().execute('delete from CachedItem where key1=? and key2 =?',  key1,  key2);
				}
			}
		}
		if ( (ret == null) && filler != null){
			String val = filler(key1, key2);
			if ( insert(key1, key2, val) ){
				ret = findMany(key1, key2)[0]
			};
			
		}
		
		return ret;
	}
	
	public boolean hasValue(String key1, String key2) {
		def row = getSql().rows("select key1 from CachedItem where key1=:foo and key2 =:bar", [foo:key1, bar: "${key2}"])
		return row.size() > 0;
	}

	public boolean hasValueForKey2HigherThan(String key1, String key2, String typeStartsWith) {
		String str = "select key1 from CachedItem where key1='${key1}' and key2 >'${key2}' and key2 like '${typeStartsWith}%'";//, [foo:key1, bar: "${key2}"]
		def row = getSql().rows(str)
		return row.size() > 0;
	}
	
	public boolean hasValueForKey2(String key2) {
		String sql = "select key1 from CachedItem where key2 ='${key2}'"
		def row = getSql().rows(sql)
		return row.size() > 0;
	}
	
	public List<CachedItem> findMany(String key1, String key2) {
		String typeWildCard = "${key2}%"
		def rows = getSql().rows("select * from CachedItem where key1=:foo and key2 like :bar", [foo:key1, bar: typeWildCard])
		rows = rows.collect{ new CachedItem(it) }
		return rows;
	}
	public List<CachedItem> findManyForKey1(String key1) {
		def rows = getSql().rows('select * from CachedItem where key1=?', key1 )
		rows = rows.collect{ new CachedItem(it) }
		return rows;
	}
	
	public List<CachedItem> findManyForKey2(String key1) {
		def rows = getSql().rows('select * from CachedItem where key2=?', key1 )
		rows = rows.collect{ new CachedItem(it) }
		return rows;
	}
	
	/**
	 * Saves an entry of the specified key2 of the specified key1
	 */
	public boolean save(String key1, String key2, String value, int status=2) {
		try{
		def row = getSql().rows('select * from CachedItem where key1=:foo AND key2=:bar', [foo:key1, bar:key2])
		if ( row.size() == 0) {
			LOGGER.info "Inserting data for key1 ${key1}/${key2}"
			return insert(key1, key2, value,status)
		}else{
			int ret = sql.executeUpdate("update CachedItem set value=:value,updatedAt=:when, status=:status where key1=:foo AND key2=:bar",[value:value, when: Sql.TIMESTAMP(new Date()),status: status,foo:key1, bar:key2])
			if (ret > 0) return true;
		}
		}catch(Exception ex){
			this.init_();
		}
		return false;
	}

	public void delete(String key1, String key2){
		getSql().execute("delete from ${tableName} where key1=? and key2 =?",  key1,  key2);
	}
	
	
	private boolean insert(String key1, String key2, String value, int status=2){
		def ret = sql.executeInsert("insert into ${tableName} (key1,key2,value,updatedAt,status) values(?,?,?,?,?)", key1, key2, value, Sql.TIMESTAMP(new Date()),status);
		if ( ret.size() > 0) return true;
	}
	/**
	 * Saves an CachedItem
	 */
	public boolean save(CachedItem ci) {
		Date dd = new Date()
		try{
		int ret = sql.executeUpdate("update CachedItem set value=?,updatedAt=?, status=? where uid=?",ci.value, Sql.TIMESTAMP(dd), ci.status,ci.uid)
		if (ret > 0) {
			ci.updatedAt = dd
			return true
		};
		}catch(Exception ex){
			this.init_();
		}
		return false;
	}

	
	public void finalize() {
		if ( sql != null) {
			sql.close()
		}
	}
	
	public void forEachCachedItem(String sql, Closure closure)
	{
		def row = getSql().rows("select * from CachedItem "+ sql);
		row.each{aRow->
			closure(new CachedItem(aRow))
		}
	}
	
	public static Cache createHsqlDBCache(String path , String tableName="CachedItem")
	{
		String url = "jdbc:h2:file:${path};MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
		return new Cache(url, "sa", "", "org.h2.Driver",tableName) ;
	}

}
