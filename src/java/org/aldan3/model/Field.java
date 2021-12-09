/* aldan3 - DataObject.java
 * Copyright (C) 1999-2007 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: Field.java,v 1.5 2011/03/30 01:12:09 dmitriy Exp $                
 *  Created on Feb 6, 2007
 *  @author Dmitriy
 */
package org.aldan3.model;

/** Defines storage field
 * 
 * @author Dmitriy
 *
 */
public interface Field {
	/** Field name
	 * 
	 * @return
	 */
    public String getName();

    /** Storage field name, usually the same as getName
     * 
     * @return
     */
    public String getStoredName();

    /** Name of field in forms, usually same as getName
     * 
     * @return
     */
    public String getWebId();
    
    /** Sql of field, in this case used for all operations
     * 
     * @return
     */
    public String getSql();
    
    /** If the field used for query
     * 
     * @return
     */
    public boolean isKey();
    
    /** Size of field in storage
     * 
     * @return
     */
    public int getSize();
    
    /** Type of field in storage
     * 
     * @return
     */
    public String getType();
    
    /** Type of field in storage
     * 
     * @return java.sqlType
     */

    public int getJDBCType();
    
    /** Precision of size
     * 
     * @return
     */
    public int getPrecision();
    
    /** Build index on the field
     * 
     * @return
     */
    public boolean isIndex();
    
    /** Add uniqueness constraint to the field
     * 
     * @return
     */
    public boolean isUnique();
    
    /** The field is foreign key
     * 
     * @return
     */
    public boolean isForeign();
    
    /** field is auto incremented key with start value and increment
     * 
     * @return
     */
    public int autoIncremented();
}