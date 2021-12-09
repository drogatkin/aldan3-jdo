/* aldan3 - ServiceProvider.java
 * Copyright (C) 1999-2009 Dmitriy Rogatkin.  All rights reserved.
 *  $Id: ServiceProvider.java,v 1.5 2013/03/02 09:15:25 cvs Exp $                
 *  Created on Jun 23, 2009
 *  @author Dmitriy
 */
package  org.aldan3.model;

/** This interface all service providers have to implemented
 * <p>
 * There is also possibility to do not implement the interface but mar method 
 * belonging to this interface by annotations
 * 
 * @author Dmitriy
 *
 */
public interface  ServiceProvider <T> {
    /** Returns preferable name of service
     * 
     * @return name of service
     */
    String getPreferredServiceName();
    
    /** Returns actual service if it was wrapped for ServiceProvider interface
     * 
     * @return actual service provider object
     */
    T getServiceProvider();
}
