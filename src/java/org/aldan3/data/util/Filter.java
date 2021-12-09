/* aldan3 - Filler.java
 * Copyright (C) 1999-2010 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: Filter.java,v 1.3 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Apr 1, 2010
 *  @author Dmitriy R
 */
package org.aldan3.data.util;

/** The interface is used for creating filters 
 * 
 * @author dmitriy
 *
 * @param <T> filter data type
 * @param <I> model or other object keeping values required for a filter execution
 * Note that filter can get also certain values injected
 */
public interface Filter <T,I>{
	/** returns name of filter expression
	 * 
	 * @return
	 */
	String getName();
	
	/** returns substituted filter value
	 * 
	 */
	T getValue(I coord);

}
