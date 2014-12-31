/* aldan3 - DataObject.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: DOFactory.java,v 1.2 2010/06/05 04:58:24 dmitriy Exp $                
 *  Created on Jun 20, 2009
 *  @author Dmitriy
 */
package org.aldan3.model;

public interface DOFactory <D extends DataObject>{
	/** creates data object which can be filled as part of storage operations
	 * 
	 * @return a new data object
	 */
	D create();
}
