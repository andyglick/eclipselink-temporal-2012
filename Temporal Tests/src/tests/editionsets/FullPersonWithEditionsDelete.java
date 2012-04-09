/*******************************************************************************
 * Copyright (c) 2011-2012 Oracle. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 and Eclipse Distribution License v. 1.0 which accompanies
 * this distribution. The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution
 * License is available at http://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * Contributors: dclarke - Bug 361016: Future Versions Examples
 ******************************************************************************/
package tests.editionsets;

import static example.PersonModelExample.T2;
import static example.PersonModelExample.T4;
import junit.framework.Assert;
import model.Address;
import model.Person;

import org.junit.Test;

import temporal.TemporalEntityManager;
import tests.FullPersonWithEditions;

/**
 * Tests change propagation through future editions.
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class FullPersonWithEditionsDelete extends FullPersonWithEditions {

    @Test
    public void deleteT2() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        em.remove(em.getEditionSet());

        Person personT2 = em.find(Person.class, getSample().getContinuityId());
        Assert.assertNull(personT2);
        Address addressT2 = em.find(Address.class, getSample().getAddress().getContinuityId());
        Assert.assertNull(addressT2);

        em.getTransaction().commit();

        personT2 = em.find(Person.class, getSample().getContinuityId());
        Assert.assertNull(personT2);
        addressT2 = em.find(Address.class, getSample().getAddress().getContinuityId());
        Assert.assertNull(addressT2);

        closeEMF();
    }

    @Test
    public void deleteT4() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        em.remove(em.getEditionSet());

        em.flush();
        
        closeEMF();
    }
}
