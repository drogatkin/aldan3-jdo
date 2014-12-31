/* aldan3 - TemplateProcessor.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: TemplateProcessor.java,v 1.4 2009/09/23 07:01:09 dmitriy Exp $                
 *  Created on Feb 5, 2007
 *  @author dmitriy
 */
package org.aldan3.model;

import java.io.Writer;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public interface TemplateProcessor {
// TODO make it returning Write
	void process(Writer pw, String template, Object model, Properties properties, Locale locale, TimeZone tz) throws ProcessException ;
	
}
