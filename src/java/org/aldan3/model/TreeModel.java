/* aldan3 - Traversable.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: TreeModel.java,v 1.1 2007/02/09 07:23:43 rogatkin Exp $                
 *  Created on Feb 8, 2007
 *  @author dmitriy
 */
package org.aldan3.model;

import java.util.List;

public interface TreeModel {
	/**
	 * returns list of children of specified parent. If parent == null allows to
	 * start tree traversing considering as root access
	 * 
	 * @param _parent  object
	 * 
	 * @return list of children empty list if no children <br>null if invalid parent
	 *         or hide the node
	 */
	public List getChildren(Object _parent);

	/**
	 * returns name of the node or the element
	 * 
	 * @return name of the node or the element, can't be null
	 */
	public String getLabel(Object _object);

	/**
	 * returns id of the node or the element to be unique over the tree
	 * 
	 * @return name of the node or the element, can't be null
	 */
	public String getId(Object _object);

	/**
	 * returns href for node or element that point to selected page.
	 * node/element jump
	 * 
	 * @param object  of element/node, can be null
	 * @return any string suitable for using in &gt;a href="
	 */
	public String getAssociatedReference(Object _object);

	/**
	 * returns image modifier for elements only to customize element appearances
	 * in dependency on element's type.
	 * 
	 * @param object of element/node, can't be null
	 * @return image modifier suffix or null if standard image is suitable
	 */
	public String getImageModifier(Object _object);

	/**
	 * return a string suitable for &gt;a href=" to redisplay current page with
	 * closing/opening selected node
	 * 
	 * @param optional
	 *            object if page depends on it, can be null
	 * @return a string suitable for &gt;a href="
	 */
	public String getSwitchReference(Object _object);

	/**
	 * checks if node/element can be reperesent this parameter string, for
	 * example, a string can be node id
	 * 
	 * @param node/element
	 *            object checking for equivalent to a string
	 * @param parameter
	 *            string
	 * @return true if object is equal
	 */
	public boolean isId(Object _object, String _object2);
	
	/** Tells if element in a given state can be marked
	 * @param node/element
	 * @param state to be drawn 
	 * @return true if can be marked
	 */
	public boolean canMark(Object _object, boolean _opened);

	/** Return tooltip text when mouse over
	 * @param node/element
	 * @return tooltip text or null if none
	 */
	public String getToolTip(Object _object);

}
