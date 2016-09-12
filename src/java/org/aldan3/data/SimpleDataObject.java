/* aldan3 - SimpleDataObject.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: SimpleDataObject.java,v 1.10 2011/06/22 02:49:56 dmitriy Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.aldan3.data.util.Filler;
import org.aldan3.model.DataObject;
import org.aldan3.model.Field;

public class SimpleDataObject implements DataObject {
	private HashMap<Field, Object> data;

	private HashMap<String, Field> fields;

	public SimpleDataObject() {
		data = new HashMap<Field, Object>();
		fields = new HashMap<String, Field>();
	}

	//	public void setName(String name) {
	//	
	//}

	@Override
	public Field defineField(Field field) {
		if (fields.containsKey(field.getName()) == false)
			fields.put(field.getName(), field);
		return field;
	}

	@Override
	public Object getField(String name) {
		return get(name);
	}

	@Override
	public Object get(String name) {
		return data.get(fields.get(name));
	}

	@Override
	public Field getField(Field field) {
		return fields.get(field.getName());
	}

	@Override
	public Set<String> getFieldNames() {
		return fields.keySet();
	}

	@Override
	public Set<Field> getFields() {
		return new HashSet<Field>(fields.values());
	}

	@Override
	public Object modifyField(String name, Object value) {
		return put(name, value);		
	}

	@Override
	public Object modifyField(Field field, Object value) {
		return put(field, value);
	}
	
		@Override
		public Object put(Field field, Object value) {
		if (value != null)
			switch (field.getJDBCType()) {
			case java.sql.Types.INTEGER:
			case java.sql.Types.NUMERIC:
				if (value instanceof Integer == false && value instanceof Long == false) {
					String sv = value.toString().trim();
					if (sv.length() == 0)
						value = new Integer(0);
					else
						value = Long.valueOf(sv);
				}
				break;
			case java.sql.Types.BIGINT:
				if (value instanceof Integer == false && value instanceof BigInteger == false
						&& value instanceof Long == false) {
					String sv = value.toString().trim();
					if (sv.length() == 0)
						value = BigInteger.valueOf(0);
					else
						value = new BigInteger(value.toString());
				}
				break;
			case java.sql.Types.REAL:
			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.FLOAT:
				if (value instanceof Integer == false && value instanceof Float == false && value instanceof Long == false
						&& value instanceof Double == false) {
					String sv = value.toString().trim();
					if (sv.length() == 0)
						value = new Float(0);
					else
						value = Double.valueOf(sv);
				}
				break;
			}
		return data.put(field, value);
	}

	@Override
	public Field removeField(Field field) {
		return removeData(fields.remove(field));
	}

	protected Field removeData(Field f) {
		if (f != null)
			data.remove(f);
		return f;
	}

	public void fillAll(Filler filler) {
		if (fields != null) {
			for (Field f : fields.values()) {
				filler.fill(f);
			}
		}
	}

	@Override
	public boolean containData(String name) {
		return isOperational(name);
	}

	@Override
	public String getName() {
		String n = getClass().getName();
		int ldp = n.lastIndexOf('.');
		return ldp >= 0 ? n.substring(ldp + 1) : n;
	}

	@Override
	public String toString() {
		if (fields == null)
			return "null";
		String s = "";
		for (Field f : fields.values()) {
			s += f.getName();
			s += ":";
			if (data.containsKey(f.getName()))
				s += data.get(f.getName());
			else
				s += "<UNDEFINED>";
			s += ", ";
		}
		return s;
	}

	@Override
	public boolean meanFieldFilter(String name) {
		return isOperational(name);
	}

	@Override
	public Object put(String name, Object value) {
		Field f = fields.get(name);
		//new IllegalArgumentException("No field "+name).printStackTrace();
		if (f == null)
			throw new IllegalArgumentException("No field "+name+ " in : "+fields);
		return put(f, value);
	}

	@Override
	public String getSql(Field field) {
		return field.getSql();
	}

	@Override
	public boolean isOperational(String name) {
		Field f = fields.get(name);
		if (data.containsKey(f))
			return true;
		return f != null && f.getSql() != null && f.getSql().length() > 0;
	}
}
