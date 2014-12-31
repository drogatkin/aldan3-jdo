/* aldan3 - ProcessException.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: ProcessException.java,v 1.2 2009/01/24 07:05:36 dmitriy Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.model;

public class ProcessException extends Exception {
	public ProcessException() {
		
	}
	
	public ProcessException(String message) {
		super(message);
	}
	
	public ProcessException(String message, Throwable reason) {
		super(message, reason);
	}
}
