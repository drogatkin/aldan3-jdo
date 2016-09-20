/* aldan3 - DBField.java
 * Copyright (C) 1999-2010 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DataRelation.java,v 1.2 2010/04/02 05:57:27 dmitriy Exp $                
 *  Created on Mar 20, 2010
 *  @author Dmitriy
 */
package org.aldan3.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aldan3.data.util.Filter;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
public @interface DataRelation {
	/** provides query for retrieving data
	 * 
	 * @return
	 */
	String query() default "";

	/** provides names of key fields
	 * 
	 * @return
	 */
	String[] keys() default {};

	/** provides name of table used for auto generate queries from object
	 * 
	 * @return
	 */
	String table() default "";
	
	/** defines applicable filters
	 * 
	 * @return
	 */
	Class <? extends Filter>[] filters() default {};
	
	/** allows to specify character set on a table level
	 * 
	 * @return
	 */
	String charset() default "";
	
}
