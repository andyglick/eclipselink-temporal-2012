/*******************************************************************************
 * Copyright (c) 2011-2012 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *      dclarke - Bug 361016: Future Versions Examples
 ******************************************************************************/
package temporal.ejb;

import java.util.List;

import javax.ejb.Local;

import model.Person;

@Local
public interface PersonService {

    List<Person> getAllCurrent();

    List<Person> getAllAtT2();

    List<Person> getAllEditions();

    void create(long id, String name, long effective);
}
