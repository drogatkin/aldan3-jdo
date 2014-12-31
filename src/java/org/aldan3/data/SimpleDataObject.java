/* aldan3 - SimpleDataObject.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: SimpleDataObject.java,v 1.10 2011/06/22 02:49:56 dmitriy Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.aldan3.data.util.Filler;
import org.aldan3.model.DataObject;
import org.aldan3.model.Field;

public class SimpleDataObject implements DataObject {
	private HashMap<String, Object> data;

	private HashSet<Field> fields;

	public SimpleDataObject() {
		data = new HashMap<String, Object>();
		fields = new HashSet<Field>();
	}

	//	public void setName(String name) {
	//	
	//}

	@Override
	public Field defineField(Field field) {
		if (fields.contains(field) == false)
			fields.add(field);
		return field;
	}

	@Override
	public Object getField(String name) {
		return get(name);
	}

	@Override
	public Object get(String name) {
		return data.get(name);
	}


	@Override
	public Field getField(Field field) {
		if (fields.contains(field) == false)
			return null;
		return field;
	}

	@Override
	public Set<String> getFieldNames() {
		HashSet<String> names = new HashSet<String>();
		for (Field f : fields)
			names.add(f.getName());
		return names;
	}

	@Override
	public Set<Field> getFields() {
		return fields;
	}

	@Override
	public Object modifyField(String name, Object value) {
		return data.put(name, value);
	}

	@Override
	public Object modifyField(Field field, Object value) {
		return data.put(field.getName(), value);
	}

	@Override
	public Field removeField(Field field) {
		if (fields.remove(field))
			return field;
		return null;
	}

	public void fillAll(Filler filler) {
		if (fields != null) {
			for (Field f : fields) {
				filler.fill(f);
			}
		}
	}

	@Override
	public boolean containData(String name) {
		if (data.containsKey(name))
			return true;
		for (Field f : fields)
			if (f.getSql() != null && f.getSql().length() > 0)
				return true;
		return false;
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
		for (Field f : fields) {
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
		return containData(name);
	}
}
