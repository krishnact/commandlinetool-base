package org.himalay.persist

import java.sql.ResultSet
import java.sql.ResultSetMetaData;

import groovy.sql.GroovyResultSet;
import groovy.sql.GroovyRowResult;

class Table {
	def table = [:].withDefault {[:]}
	
	String row;
	String col;
	Closure header = {return it.toString()};

	String  topLeft = $/ /$
	String  keyColumnClass = 'keyColumnClass'
	String  tableName = ''
	String  csvDelim=","
	/**
	 * This closure will be invoked for each cell. The argument will be a map with two additional members:<br />
	 * row: The name of the row <br />
	 * col: The name of the column<br />
	 */
	def eachCell = {it->
		return it.value==null? it.toString() : it.value.toString()
	}
	/**
	 * Set this closure to change the order of columns
	 */
	def orderColumns = {it->
		return it
	}
	public Table(String row, String col, boolean useUpperCase = true) {
		super();
		this.row = row;
		this.col = col;
		if (useUpperCase)
		{
			this.row = this.row.toUpperCase()
			this.col = this.col.toUpperCase()
		}
	}
	/**
	 * Create a table with 'row' and 'column' as row and column names
	 */
	public Table() {
		this('row','column', false);
	}
	
	/*
	 * 
	 * Add a groovy result row
	 */
	public void add(GroovyRowResult  aRow)
	{
		Object colVal = aRow.get(col)
		Object rowVal = aRow.get(row)
		def cellVal = aRow.findAll{!( it.key == col || it.key == row)}.collectEntries {
			if (!( it.key == col || it.key == row))
			{
				return [it.key,  it.value]
			}
		}
		
		table[rowVal][colVal] = cellVal
	}

	/**
	 * Add a GroovyResultSet
	 * @param aRow
	 */
	public void add(GroovyResultSet aRow)
	{
		Object colVal = aRow.getAt(col)
		Object rowVal = aRow.getAt(row)
		
		ResultSetMetaData mt = aRow.getMetaData();
		int cols = mt.getColumnCount()
		def columns = (1..cols).collect{
			mt.getColumnName(it)
		}.findAll{!( it == col || it == row)}
		
		def cellVal = [:] 
		columns.each{colName ->
			cellVal[colName]= aRow.getAt(colName)
		}

		table[rowVal][colVal] = cellVal
	}
	/**
	 * Add a Map
	 * <pre>
	 * {@code
	 *Table table = new Table('row','colmn')
     * table.add([row:'row1', colmn: 'col1', val: 'val1'])
	 * }
	 * </pre>
	 * @param aRow
	 */
	public void add(Map  aRow)
	{
		Object colVal = aRow.get(col)
		Object rowVal = aRow.get(row)
		def cellVal = aRow.findAll{!( it.key == col || it.key == row)}.collectEntries {
			if (!( it.key == col || it.key == row))
			{
				return [it.key,  it.value]
			}
		}
	
		table[rowVal][colVal] = cellVal
	}
	
	/**
	 * Place values at row and column
	 * <pre>
	 * {@code
	 * 	Table table = new Table('row','colmn')
	 *	table.addRowColumn('row1','col1','1-1')
	 *	table.addRowColumn('row2','col2','2-2')
	 *	table.addRowColumn('row3','col3','3-3')
	 * }
	 * </pre>
	 * @param row
	 * @param col
	 * @param value
	 */
	public void addRowColumn(String row, String col, String value){
		def val =[:]
		val[this.row] = row
		val[this.col] = col
		val['value']  = value
		add (val)
	}
	
	public String toString()
	{
		// Find all columns
		def columns1 = table.values().collectMany{
			it.keySet()
		}.unique()
		
		// Print header
		String retVal = "," + columns1.join(",") +"\n";
		retVal += table.collect{ it->
			it.key +"," +columns1.collect{col-> 
				it.value[col]
			}.join(",")
		}.join("\n")
		
	}
	
	/**
	 * Convert to a CSV.
	 * <pre> 
	 * {@code
	 * 	table.toCSV{it->
	 *		return "${it.row}x${it.col}=${it.value}"
	 *        }
	 * </pre>
	 * @param eachCell A closure that will be invoked for each cell
	 * @return The CSV as text
	 */
	public String toCSV(Closure eachCell=null)
	{
		// Find all columns
		def columns = table.values().collectMany{
			it.keySet()
		}.unique()
		columns = orderColumns(columns)
		
		// Print header
		String retVal = this.row +this.csvDelim + columns.collect{header(it)}.join(this.csvDelim) +"\n"
		retVal += table.collect{ it->
			it.key+this.csvDelim + columns.collect{col->
				if ( eachCell == null)
				{
					return it.value[col]
				}
					def cellVal = it.value[col]
					if ( cellVal != null){
					cellVal['row'] = it.key
					cellVal['col'] = col
						eachCell(cellVal)
					}else{
						""
					}
				
			}.join(csvDelim)
		}.join("\n")
		
	}
	/**
	 * Convert to a HTML table
	 * <pre> 
	 * {@code
	 * 	table.toHTML(table:[border:'1']){it->
	 *		return "${it.row}x${it.col}=${it.value}"
	 *        }
	 * </pre>
	 * }
	 * </pre>
	 * @param attributes
	 * @return
	 */
	public String toHTML(Map attributes, Closure eachCell=null)
	{
		def tagAttributes = [:].withDefault {""}
		if ( eachCell == null)
		{
			eachCell = this.eachCell
		}
		
		attributes?.each { tagAttr->
			String tagName = tagAttr.key
			Map    attr    = tagAttr.value
			String attrStr = attr.collect{
				"${it.key}='${it.value}'"
			}.join(" ")
			
			tagAttributes[ tagName]= attrStr
		}
		// Find all columns
		def columns = table.values().collectMany{
			it.keySet()
		}.unique()
		
		columns = orderColumns(columns)

		
		// Print header
		def  tags = []
		tags << "table"  
		String retVal = "<table ${tagAttributes[tags.last()]}>"
		retVal +=
				"\n<thead class='${tableName}_thead'>"+
				"<tr>"+ 
				"<td class='${keyColumnClass} topleftClass'>${this.topLeft}</td>" + 
				columns.collect{"<td class='col_${it} ${tableName}_header_cell'>${this.header(it)}</td>"}.join("") +
				"</tr></thead>"
		retVal += "\n<tbody class='${tableName}_tbody'>"
		int trIdx = 1;
		retVal += table.collect{ it->
			int tdIdx = 1;
			"<tr id='${trIdx}'><td class='${keyColumnClass}'>"+it.key +"</td>" +columns.collect{col->
				String vv = ""
					def cellVal = it.value[col]
					if ( cellVal != null){
					cellVal['row'] = it.key
					cellVal['col'] = col
					vv = eachCell.call(cellVal)
					}else{
					vv = ""
					}
			
				return "<td class='col_${col}'>${vv}</td>"
			}.join("") +"</tr>\n" 
		}.join("") ;
		retVal += "</tbody>";
		retVal +="\n</table>"
		return retVal
	}

	/**
	 * Place a column
	 * <pre>
	 * {@code
	 * 	Table table = new Table('row','colmn') 
	 *	table.addColumn('Jeremy',['math','reading'])
	 *	table.addColumn('Bob',['math','reading','social'])
	 * 	table.addColumn('Alice',['math','reading','science'])
	 *	table.topLeft = 'Favorite Subjects'
	 * }
	 * </pre>
	 * @param colName
	 * @param data
	 */
	public void addColumn(String colName, List data)
	{
		data.eachWithIndex {aData,idx->
			addRowColumn(""+idx,colName,aData)
		}
	}
	
	/**
	 * Getter for internal map
	 * @return
	 */
	public def internalMap(){
		return this.table;
	}
}
