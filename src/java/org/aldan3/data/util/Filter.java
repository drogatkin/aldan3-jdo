/* aldan3 - Filler.java
 * Copyright (C) 1999-2010 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: Filter.java,v 1.3 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Apr 1, 2010
 *  @author Dmitriy R
 */
package org.aldan3.data.util;

import org.aldan3.model.Coordinator;

public interface Filter <T>{
	
	String getName();
	
	T getValue(Coordinator coord);

}
