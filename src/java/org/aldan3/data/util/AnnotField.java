/* aldan3 - AnnotField.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: AnnotField.java,v 1.14 2013/04/29 05:45:50 cvs Exp $                
 *  Created on Jun 18, 2009
 *  @author Dmitriy
 */
package org.aldan3.data.util;

import java.io.File;
import java.util.Date;

import org.aldan3.annot.DBField;
import org.aldan3.annot.FormField;
import org.aldan3.model.Field;

/**
 * Implements db field based on Java annotation
 * 
 * @author Dmitriy
 * 
 *         Current implementation is MySQL specific, type of fields have to be
 *         taken from DB vendor specific mapping
 */
public class AnnotField implements Field {
	private String name;

	private DBField dbField;

	private FormField ff;

	private Class<?> objectType;

	public AnnotField(java.lang.reflect.Field field) {
		dbField = field.getAnnotation(DBField.class);
		ff = field.getAnnotation(FormField.class);
		if (dbField == null && ff == null)
			throw new IllegalArgumentException("No annotations available");
		name = ff != null ? ff.dbFieldName() : "";
		if (name.length() == 0)
			name = field.getName();
		objectType = field.getType();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPrecision() {
		return dbField != null ? dbField.precision() : 0;
	}

	@Override
	public int getSize() {
		if (dbField != null && dbField.size() > 0)
			return dbField.size();
		if (ff != null && ff.presentSize() > 0)
			return ff.presentSize();
		if (objectType == Date.class)
			return 0;
		return 1;
	}

	@Override
	public String getSql() {
		if (dbField != null && dbField.sql().length() > 0)
			return dbField.sql();
		return null;
	}

	@Override
	public String getStoredName() {
		if (dbField != null)
			return name;
		return ff.dbFieldName();
	}

	// TODO type mapping supposes to be flexible and configurable to DB vendor specific
	@Override
	public String getType() {
		String type = dbField != null ? dbField.type() : "";
		if (type.length() > 0)
			return type;
		if (objectType == String.class) {
			if (getSize() < 256)
				return "varchar";
			else
				return "TEXT";
		} else if (objectType == Date.class) {
			if (autoIncremented() == -1)
				return "TIMESTAMP default current_timestamp";
			return "datetime";
		} else if (objectType == File.class)
			return "BLOB";
		//else {
			
		//}
		if (objectType.isPrimitive()) {
			if (objectType == int.class)
				if (autoIncremented() > 0)
					return "int NOT NULL AUTO_INCREMENT";
				else
					return "int";
			else if (objectType == long.class)
				return "bigint" + (autoIncremented() > 0 ? " NOT NULL AUTO_INCREMENT" : "");
			else if (objectType == boolean.class)
				return "char";
			else if (objectType == float.class || objectType == double.class)
				return "double";
			else if (objectType == byte.class)
				return "char";
			else if (objectType == char.class)
				return "char";
		} else if (objectType.isArray()) {
			if (objectType.getComponentType() == byte.class)
				return "BLOB";
			else if (objectType.getComponentType() == char.class || objectType.isEnum())
				return "varchar";
		} else if (objectType.isEnum())
			return "varchar";
		return null;
	}

	@Override
	public String getWebId() {
		if (ff != null)
			return ff.formFieldName();
		return name;
	}

	@Override
	public boolean isKey() {
		return dbField != null && dbField.key();
	}

	@Override
	public boolean isForeign() {
		if (dbField != null) {
			String[] foreignConstraints = dbField.foreign();
			return foreignConstraints.length > 0;
		}
		return false;
	}

	@Override
	public boolean isIndex() {
		return dbField != null && dbField.index();
	}

	@Override
	public boolean isUnique() {
		return dbField != null && dbField.unique();
	}

	@Override
	public int autoIncremented() {
		return dbField == null ? 0 : dbField.auto();
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ (name == null ? "".hashCode() : name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnnotField) {
			AnnotField af = (AnnotField) obj;
			return name.equals(af.name);
		}
		return false;
	}

}
