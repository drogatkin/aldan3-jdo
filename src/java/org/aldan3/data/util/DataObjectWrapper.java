/* aldan3 - DataObjectWrappert.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DataObjectWrapper.java,v 1.4 2010/04/13 04:10:05 dmitriy Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.data.util;

import java.util.Set;

import org.aldan3.model.DataObject;
import org.aldan3.model.Field;

public class DataObjectWrapper implements DataObject {
	
	private DataObject innerDO;
	public DataObjectWrapper(DataObject object) {
		innerDO = object;
	}

	@Override
	public boolean containData(String name) {
		return innerDO.containData(name);
	}

	@Override
	public boolean meanFieldFilter(String name) {
		return containData(name);
	}

	@Override
	public Field defineField(Field field) {
		return innerDO.defineField(field);
	}

	@Override
	public Object getField(String name) {
		return get(name);
	}
	
	@Override
	public Object get(String name) {
		return innerDO.get(name);
	}
	
	@Override
	public Field getField(Field field) {
		return innerDO.getField(field);
	}

	@Override
	public Set<String> getFieldNames() {
		return innerDO.getFieldNames();
	}

	@Override
	public Set<Field> getFields() {
		return innerDO.getFields();
	}

	@Override
	public String getName() {
		return innerDO.getName();
	}

	@Override
	public Object modifyField(String name, Object value) {
		return innerDO.modifyField(name, value);
	}

	@Override
	public Object modifyField(Field field, Object value) {
		return innerDO.modifyField(field, value);
	}

	@Override
	public Field removeField(Field field) {
		return innerDO.removeField(field);
	}

	public DataObject getWrappedDO() {
		return innerDO;		
	}
	
}
