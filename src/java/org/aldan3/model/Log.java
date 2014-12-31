/* aldan3 - Log.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  
 *  Visit http://aldan3.sourceforge.net to get the latest infromation
 *  about Rogatkin's products.                                                        
 *  $Id: Log.java,v 1.4 2013/04/20 05:23:55 cvs Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.model;

public abstract class Log implements ServiceProvider  {
	public static final String DEBUG = "debug";

	public static final String WARNING = "warning";

	public static final String ERROR = "error";
	
	public static final String INFO = "info";
	
	public static final String NAME = "log";
	
	public static Log l;
	
	public abstract void log(String severity, String where, String message, Throwable t, Object... details);
	
	public void debug(String message,  Object... details) {
		log(DEBUG, getCaller(new Throwable(), false), message, null, details);
	}
	
	public void error(String message, Throwable t) {
		log(ERROR, t==null?getCaller(new Throwable(), false):getCaller(t, true), message, t);
	}
	
	public void info(String message) {
		log(INFO, "", message, null);
	}

	protected String getCaller(Throwable t, boolean last) {
		StackTraceElement stack[] = t.getStackTrace();
		if (stack.length == 0)
			return "";
		if (last)
			return stack[0].getClassName() + '.' + stack[0].getMethodName();
		//				 First, search back to a method in the Logger class.
		int ix = 0;
		while (ix < stack.length) {
			if (stack[ix].getClassName().equals("org.aldan3.model.Log")) {
				break;
			}
			ix++;
		}
		while (ix < stack.length) {
			if (stack[ix].getClassName().equals("org.aldan3.model.Log") == false) {
				ix++;
				break;
			}
			ix++;
		}
		if (ix < stack.length)
			return stack[ix].getClassName() + '.' + stack[ix].getMethodName();
		return "";
	}

	@Override
	public String getPreferredServiceName() {
		return NAME;
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}
	
	
}
