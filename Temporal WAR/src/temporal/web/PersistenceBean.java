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
package temporal.web;

import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

import temporal.ejb.PersonService;

import model.Person;

@ManagedBean
public class PersistenceBean {

    private List<Person> current;

    private List<Person> atT2;
    
    @EJB
    private PersonService service;

    public PersonService getService() {
        return service;
    }

    public void setService(PersonService service) {
        this.service = service;
    }

    public List<Person> getCurrent() {
        if (this.current == null) {
            this.current = getService().getAllCurrent();
        }
        return this.current;
    }

    public List<Person> getAtT2() {
        if (this.atT2 == null) {
            this.atT2 = getService().getAllAtT2();
        }
        return this.atT2;
    }
}
