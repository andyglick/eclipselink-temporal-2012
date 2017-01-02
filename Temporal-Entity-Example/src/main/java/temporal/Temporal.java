/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: dclarke - Bug 361016: Future Versions Examples
 ******************************************************************************/
package temporal;


/**
 * Indicates a persistent object which is effective for a specified period of time. 
 * 
 * @see Effectivity
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public interface Temporal {

    /**
     * The range of time the persistent object is effective.
     */
    Effectivity getEffectivity();

}
