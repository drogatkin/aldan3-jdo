/* aldan3 - SimpleField.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: SimpleField.java,v 1.11 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Jun 8, 2009
 *  @author Dmitriy R
 */
package org.aldan3.data.util;

import org.aldan3.model.Field;

public class SimpleField implements Field {
	String name;
	String type = "varchar";
	int size;
	String label;
	int resutlIndex; // future use in result for identifying a field
	
	public SimpleField(String l) {
		this.label = l;
		name = l;
	}
	
	public SimpleField(String l, String n) {
		label = l;
		name = n;
	}
	
	@Override
	public String getName() {
		return label;
	}

	@Override
	public String getSql() {
		return null;
	}

	@Override
	public String getStoredName() {
		return name;
	}

	@Override
	public String getWebId() {
		return getName();
	}

	@Override
	public String toString() {
		return "Field: "+getName()+"("+getStoredName()+") of "+getType() + "("+getSize()+") "+ getSql();
	}

	@Override
	public boolean isKey() {
		return false;
	}

	@Override
	public int getPrecision() {
		return 0;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public String getType() {
		return type;
	}

	// TODO think about improving
	@Override
	public boolean equals(Object field) {
		if (field == this)
			return true;
		if (field instanceof Field)
			return name.equals(((Field)field).getName());
		return super.equals(field);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean isForeign() {
		return false;
	}

	@Override
	public boolean isIndex() {
		return false;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public int autoIncremented() {
		return 0;
	}
}
