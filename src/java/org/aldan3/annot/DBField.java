/* aldan3 - DBField.java
 * Copyright (C) 1999-2008 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DBField.java,v 1.8 2011/03/30 01:12:09 dmitriy Exp $                
 *  Created on Nov 2, 2008
 *  @author Dmitriy
 */

package org.aldan3.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aldan3.data.util.FieldConverter;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.METHOD })
public @interface DBField {
	/** defines type of field how it appears in table create SQL statement
	 * 
	 * @return
	 */
	String type() default ""; // make it enum?

	/** defines size of field in field type units
	 * 
	 * @return
	 */
	int size() default -1;

	/** tells to create index against the field
	 * 
	 * @return
	 */
	boolean index() default false;

	/** tell that the field is primary key
	 * 
	 * @return
	 */
	boolean key() default false;

	/** tells to apply uniqueness constraint
	 * 
	 * @return
	 */
	boolean unique() default false;

	/** Class name used for conversion of field object type to/from string (varchar) stored in db
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Class<? extends FieldConverter> converter() default FieldConverter.class;

	/** SQL snippet used for the field in a where clause
	 * 
	 * @return
	 */
	String sql() default "";

	/** specifies tables and corresponding primary keys of the foreign key
	 * in form "table_name(field_name)"
	 * @return arrays of tables and corresponding keys in format "table_name(key_name)"
	 */
	String[] foreign() default {};
	
	/** auto generated key, if = 0, no auto
	 * 
	 * @return auto key value generation start number, unless 0
	 */
	int auto() default 0;
	
	/** defines precision, number of digits after dot for numeric fields
	 * ignored for other
	 * @return
	 */
	int precision() default 0;
	
	/** allows to specify a specific character set for a column
	 * It is DB specific
	 * @return charset which will be applied for a column creation
	 */
	String charset() default "";
	
	/** in conjunction with unique to make case insensitive
	 * 
	 * @return
	 */
	boolean nocase () default false;
}
