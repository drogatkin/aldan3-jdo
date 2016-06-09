/* aldan3 - Sql.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: Sql.java,v 1.19 2014/03/22 03:33:18 cvs Exp $                
 *  Created on Jun 8, 2009
 *  @author Dmitriy R
 */
package org.aldan3.util;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.aldan3.data.SimpleDataObject;
import org.aldan3.data.util.SimpleField;
import org.aldan3.model.DOFactory;
import org.aldan3.model.DataObject;
import org.aldan3.model.Field;

/** Provides utility methods for SQL manipulations
 * 
 * @author Dmitriy
 *
 */
public class Sql {
	/** add quotes to string and do other escaping
	 * \0  An ASCII NUL (0x00) character.<br>
	 * \'  A single quote (&apos;) character.<br> 
	 * \"  A double quote (&quot;) character.<br>
	 * \b  A backspace character.<br> 
	 * \n  A newline (linefeed) character.<br> 
	 * \r  A carriage return character.<br> 
	 * \t  A tab character.<br> 
	 * \Z  ASCII 26 (Control-Z). See note following the table.<br> 
	 * \\  A backslash (&#92;) character.<br> 
	 * \%  A &#37; character. See note following the table.<br> 
	 * \_  A &#95; character. See note following the table.<br> 
	 * @return a string with single quote escaped
	 */
	public static String escapeQuote(String _orig) {
		// TODO make it portable between databaes 
		if (_orig == null)
			return _orig;
		StringBuffer result = new StringBuffer(_orig.length() + 10);
		for (int i = 0, n = _orig.length(); i < n; i++) {
			char c = _orig.charAt(i);
			switch (c) {
			//case '\\':
				//result.append("\\\\");
				//break;
			case '\'':
				result.append("''");
				break;
/*			case 0:
				result.append("\\0");
				break;
			case '\b':
				result.append("\\b");
				break;
			case '\n':
				result.append("\\n");
				break;
			case '\r':
				result.append("\\r");
				break;
			case '\t':
				result.append("\\t");
				break;
			case 26:
				result.append("\\Z");
				break;
			case '%':
				result.append("\\%");
				break;
			case '_':
				result.append("\\_");
				break;*/
			default:
				result.append(c);
			}
		}
		return result.toString();
	}

	/** converts Java object to corresponding SQL
	 * value with awareness of the object type
	 * @param data
	 * @param dateTimePattern
	 * @return
	 */
	public static String toSqlString(Object data, String dateTimePattern) {
		if (data == null)
			return "NULL";
		if (data instanceof String) {
			return "'" + escapeQuote((String) data) + "'";
		} else if (data instanceof Number) {
			return data.toString();
		} else if (data instanceof Boolean) {
			return ((Boolean) data).booleanValue() ? "'T'" : "'F'";
		} else if (data instanceof Date) {
			return new SimpleDateFormat(dateTimePattern).format(data);
		} else if (data instanceof Character)
			return "'" + escapeQuote("" + data) + "'";
		else if (data.getClass().isArray())
			if (data instanceof char[]) // TODO check for other primary types
				return "'" + escapeQuote(new String((char[]) data)) + "'";
			else if (data instanceof int[])
				return "'" + Arrays.toString((int[]) data) + "'";
			else if (data instanceof long[])
				return "'" + Arrays.toString((long[]) data) + "'";
			else if (data instanceof double[])
				return "'" + Arrays.toString((double[]) data) + "'";
			else
				return "'" + escapeQuote(Arrays.toString((Object[]) data)) + "'";
		return "'" + escapeQuote(data.toString()) + "'";
	}

	/** converts Java object to SQL value 
	 * 
	 * @param data
	 * @param dateTimePattern
	 * @return
	 */
	public static String toSqlValue(Object data, String dateTimePattern) {
		if (data == null)
			return "NULL";
		if (data instanceof String) {
			return escapeQuote((String) data);
		} else if (data instanceof Number) {
			return data.toString();
		} else if (data instanceof Boolean) {
			return ((Boolean) data).booleanValue() ? "T" : "F";
		} else if (data instanceof Date) {
			return new SimpleDateFormat(dateTimePattern).format(data);
		} else if (data instanceof Character)
			return "'" + escapeQuote("" + data) + "'";
		else if (data.getClass().isArray()) {
			if (data instanceof char[]) // TODO check for other primary types
				return escapeQuote(new String((char[]) data));
			else if (data instanceof int[])
				return Arrays.toString((int[]) data); // TODO make all numbers explicit 4 digits with leading zeros for assure uniquness, like 0023, 0001
			else if (data instanceof long[])
				return Arrays.toString((long[]) data);
			else if (data instanceof double[])
				return Arrays.toString((double[]) data);
			else
				return escapeQuote(Arrays.toString((Object[]) data));
		}
		return data.toString();
	}
	
	public static Object toPreparedSqlValue(Object data) {
		if (data instanceof Boolean) 
			return ((Boolean) data).booleanValue() ? "T" : "F";
		else if (data instanceof Character)
			return data.toString();
		return data;
	}

	public static void fillDO(ResultSet rs, DataObject dataObject) throws SQLException {
		Set<Field> fields = dataObject.getFields();
		for (Field f : fields) {
			dataObject.modifyField(f, rs.getObject(f.getStoredName()));
		}
	}

	public static DataObject createDO(ResultSet rs) throws SQLException {
		return createDO(rs, null);
	}

	public static <D extends DataObject> D createDO(ResultSet rs, DOFactory<D> factory) throws SQLException {
		D result = (D) (factory == null ? new SimpleDataObject() : factory.create());
		if (result == null)
			throw new IllegalArgumentException("Factory "+factory+" couldn't create an object");
		ResultSetMetaData metadata = rs.getMetaData();
		for (int c = 1, cc = metadata.getColumnCount(); c <= cc; c++) {
			Field f = new SimpleField(metadata.getColumnLabel(c), metadata.getColumnName(c));
			if (result.getField(f) == null)
				result.defineField(f);
			int type = metadata.getColumnType(c);
			if (type == Types.DATE || type == Types.TIMESTAMP || type == Types.TIME)
				result.modifyField(f, rs.getTimestamp(c));
			else if (type == Types.CLOB || type == Types.LONGNVARCHAR || type == Types.LONGVARCHAR) {
				Clob clob = null;
				try {
					clob = rs.getClob(c);
					if (clob != null) // TODO throw an exception if CLOB is too big
						result.modifyField(f, clob.getSubString(1, (int)clob.length()));
					else
						result.modifyField(f, null);
				} finally {
					if (clob != null)
						clob.free();
				}
			} else
				result.modifyField(f, rs.getObject(c));
		}
		return result;
	}

}
