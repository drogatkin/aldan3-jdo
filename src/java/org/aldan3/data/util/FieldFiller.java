/* aldan3 - FieldFiller.java
 * Copyright (C) 1999-2011 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: FieldFiller.java,v 1.3 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Jun 21, 2009
 *  @author Dmitriy R
 */

package org.aldan3.data.util;

public interface FieldFiller<T, S> {
	T fill(S modelObject, String filter);
}
