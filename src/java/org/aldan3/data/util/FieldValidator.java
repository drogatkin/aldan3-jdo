/* aldan3 - FromField.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: FieldValidator.java,v 1.2 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Jun 8, 2009
 *  @author Dmitriy R
 */
package org.aldan3.data.util;

public interface FieldValidator<T> {
	void validate(T value);
}
