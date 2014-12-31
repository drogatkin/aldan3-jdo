/* aldan3 - AccessControl.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: AccessControl.java,v 1.2 2009/08/04 22:35:38 dmitriy Exp $                
 *  Created on Jun 23, 2009
 *  @author Dmitriy
 */
package  org.aldan3.model;

/** Security manager used for resource accessing
 * 
 * @author dmitriy
 *
 */
public interface AccessControl {
	/** Checks if resource can be accessed
	 * 
	 * @param name of resource
	 * @param requester of resource
	 * @throws SecurityException if access not granted
	 */
	public void check(String name, Object requester) throws SecurityException;
}