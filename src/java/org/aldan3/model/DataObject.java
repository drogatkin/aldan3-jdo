/* aldan3 - DataObject.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DataObject.java,v 1.11 2013/04/18 23:01:33 cvs Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.model;

import java.util.Set;

public interface DataObject {
	/** returns a field value as an object
	 * 
	 * @param name
	 * @return value
	 * @deprecated and method get has to be used
	 */
	@Deprecated
	public Object getField(String name);
	
	/** returns a field value as an object
	 * method <strong>getField</strong> for the same purpose is depreciated
	 * 
	 * @param name
	 * @return
	 */
	public Object get(String name); // TODO reconsider public <T> T get(String name);

	/** Sets a new object field value, no reflection in a persistent storage 
	 *  
	 * @param name
	 * @param value
	 * @return
	 */
	//public Object put(String name, Object value);
	
	/** Sets a new object field value, no reflection in a persistent storage 
	 *  
	 * @param name
	 * @param value
	 * @return
	 */
	public Object modifyField(String name, Object value);

	/** Sets a new object value for a field using Field object for field addressing, no
	 * reflection in persistent storage
	 * @param field
	 * @param value
	 * @return
	 */
	public Object modifyField(Field field, Object value);

	/** Returns names of all fields as set of strings
	 * 
	 * @return
	 */
	public Set<String> getFieldNames();

	/** Gets all fields of  data object as a set of fields
	 * 
	 * @return
	 */
	public Set<Field> getFields();

	/** Defines a new field in data object or modify existing one
	 * 
	 * @param field
	 * @return
	 */
	public Field defineField(Field field);

	/** removes a field from data object
	 * a removed field won't be visible any more in operations
	 * 
	 * @param field
	 * @return
	 */
	public Field removeField(Field field);

	/** Gets a field definition using a field for addressing
	 * 
	 * @param field
	 * @return
	 */
	public Field getField(Field field);

	/** Tells if object contain any data for field key or processing including null
	 * 
	 * @param name field name (key)
	 * @return true if data are in data object
	 * @Deprecated see <strong>meanFieldFilter</strong>  
	 */	
	@Deprecated
	public boolean containData(String name);
	
	/** Provides filtering fields not containing meaningful data
	 * for the current operation
	 * @param field name
	 * @return true if the field value has to be included in an operation otherwise false
	 * This method replaces  containData which is depreciated
	 */
	public boolean meanFieldFilter(String name);
	
	/** specifies if the field is a part of operation, like key, where condition
	 * 
	 * @param name
	 * @return
	 */
	//public boolean isOperational(String name);
		
	/** tells if the parameter field name is a key 
	 * 
	 * @param name
	 * @return
	 */
	//public boolean isKey(String name);
	
	/** Gets name of data object, it is a name in persistent storage, usually table or view name
	 * 
	 * @return
	 */
	public String getName();
}
