/* aldan3 - Coordinator.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: Coordinator.java,v 1.2 2009/08/04 22:35:38 dmitriy Exp $                
 *  Created on Jun 23, 2009
 *  @author Dmitriy
 */
package org.aldan3.model;

import org.aldan3.data.DOService;

public interface Coordinator {
	public static final String DOSERVICE = DOService.NAME;

	Object getModel(String name);

	Object getService(String Name);
}
