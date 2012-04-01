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

import static example.PersonModelExample.*;

import javax.persistence.RollbackException;

import junit.framework.Assert;
import model.Address;
import model.Person;

import org.junit.Test;

import temporal.TemporalEntityManager;
import tests.BaseTestCase;

/**
 * 
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class BrokenTemporalReferenceTests extends BaseTestCase {

    /**
     * SETUP 1. Create new AddressEntity to exist at T2 2. Create new
     * PersonEdition at T4 3. Reference Address@T2 from Person@T4 TEST Delete
     * EditionSet@T2 including new
     */
    @Test
    public void breakFKDeletingEarliedEntity() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        
        em.getTransaction().begin();
        Address aT2 = em.newEntity(Address.class);
        em.getTransaction().commit();
        
        Assert.assertTrue(aT2.getContinuityId() > 0);
        em.close();
        
        em = getEntityManager();
        em.setEffectiveTime(T4);
        
        em.getTransaction().begin();
        Person pT4 = em.newEntity(Person.class);
        Address aT4 = em.find(Address.class, aT2.getContinuityId());
        
        Assert.assertNotNull(aT4);
        Assert.assertEquals(T2, aT4.getEffectivity().getStart());
        
        pT4.setAddress(aT4);
        em.getTransaction().commit();
        em.close();
        
        em = getEntityManager();
        em.setEffectiveTime(T2);
        aT2 = em.find(Address.class, aT2.getContinuityId());
        
        em.getTransaction().begin();
        em.remove(aT2);
        
        try {
        em.getTransaction().commit();
        } catch (RollbackException e) {
            return;
        }
        Assert.fail("RollbackException execpted for violating FK");
    }
}
