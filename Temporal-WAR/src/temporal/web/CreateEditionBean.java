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

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

import temporal.ejb.PersonService;

@ManagedBean
public class CreateEditionBean {

    private long id;
    
    private String name;
    
    private long effective;

    @EJB
    private PersonService service;

    public PersonService getService() {
        return service;
    }

    public void setService(PersonService service) {
        this.service = service;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEffective() {
        return effective;
    }

    public void setEffective(long effective) {
        this.effective = effective;
    }

    public String create() {
        getService().create(getId(), getName(), getEffective());
        return "index?faces-redirect=true";
    }
}
