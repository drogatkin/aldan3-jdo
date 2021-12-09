/* Aldan3 - OptionMap.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: OptionMap.java,v 1.3 2011/04/15 01:29:05 dmitriy Exp $                
 *  Created on Jun 23, 2009
 *  @author Dmitriy R
 */
package org.aldan3.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
public @interface OptionMap {

	String valueMap() default "value";

	String labelMap() default "label";

	String localizedLabelMap() default "local_label";
	
	String helpMap() default "description";
}
