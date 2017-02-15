package org.himalay.persist


import groovy.transform.ToString;


import java.sql.Clob
import java.sql.SQLException;

public class CachedItem {

	long uid

	java.lang.String key1
	java.lang.String key2
	java.lang.String value
	java.util.Date updatedAt

	int status
	public CachedItem(){}
	public CachedItem(def src) {
		def val   = src.value
		if ( val instanceof Clob) {
			value     = readClob(val)
		}else{
			value = val;
		}
		key1      = src.key1 ;
		key2      = src.key2;
		updatedAt = src.updatedAt
		uid       = src.uid
		status    = src.status
	}

	@Override
	public String toString() {
		int limit = value.length();
		if ( limit > 16) limit =16;
		return "CachedItem [uid=" + uid + ", key1=" + key1 + ", key2=" + key2 + ", status=" + status + ", value=" + value.substring(0,limit) + ((limit==16) ?"..." : "" )+" , updatedAt="+ updatedAt + "]";
	}

	public static String readClob(Clob clob) throws SQLException, IOException {
		StringBuilder sb = new StringBuilder((int) clob.length());
		Reader r = clob.getCharacterStream();
		char[] cbuf = new char[2048];
		int n;
		while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
			sb.append(cbuf, 0, n);
		}
		return sb.toString();
	}

	public String getKeyTwo() {
		return key2
	}
	public String getKeyOne() {
		return key1
	}
}

