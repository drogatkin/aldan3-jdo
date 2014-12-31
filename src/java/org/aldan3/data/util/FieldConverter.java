/* Aldan3 - FromField.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: FieldConverter.java,v 1.3 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Jun 8, 2009
 *  @author Dmitriy R
 */
package org.aldan3.data.util;

import java.util.Locale;
import java.util.TimeZone;

public interface FieldConverter<T> {
	T convert(String value, TimeZone tz, Locale l);

	String deConvert(T value, TimeZone tz, Locale l);
}
