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

import static example.PersonModelExample.T1;
import static example.PersonModelExample.T2;
import static example.PersonModelExample.T3;
import static example.PersonModelExample.T4;
import static example.PersonModelExample.T5;

import javax.persistence.RollbackException;

import junit.framework.Assert;
import model.Address;
import model.Person;

import org.junit.Test;

import temporal.EditionSet;
import temporal.EditionSetHelper;
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
    public void breakFKFromEntityByDelete() {
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

    @Test
    public void breakFKFromEntityByMove() {
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
        em.setEffectiveTime(T4);
        EditionSet es = em.getEditionSet();

        Assert.assertNotNull(es);
        try {
            EditionSetHelper.move(em, T1);
        } catch (IllegalStateException e) {
            return;
        }
        Assert.fail("IllegalStateException execpted for violating FK");
    }

    /**
     * SETUP 1. Create new AddressEntity to exist at T2 2. Create new
     * PersonEdition at T4 3. Reference Address@T2 from Person@T4 TEST Delete
     * EditionSet@T2 including new
     */
    @Test
    public void breakFKFromEdition() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);

        em.getTransaction().begin();
        Address aT2 = em.newEntity(Address.class);
        Person pT2 = em.newEntity(Person.class);
        pT2.setAddress(aT2);
        em.getTransaction().commit();

        Assert.assertTrue(aT2.getContinuityId() > 0);
        em.close();

        em = getEntityManager();
        em.setEffectiveTime(T4);

        em.getTransaction().begin();
        Person pT4 = em.find(Person.class, pT2.getContinuityId());
        pT4 = em.newEdition(pT4);
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

    /**
     * Create a new Address Entity at BOT with an end of T4. Then create a
     * future Person at T2 that references the Address. Move the T2
     * {@link EditionSet} to T5 where there is no valid Address.
     */
    @Test
    public void moveEditionSetBeyondReferencedEnd() {
        TemporalEntityManager em = getEntityManager();
        em.getTransaction().begin();
        Address address = em.newEntity(Address.class);
        address.getEffectivity().setEnd(T4);
        em.getTransaction().commit();
        em.close();

        em = getEntityManager();
        em.setEffectiveTime(T2);

        em.getTransaction().begin();
        Address aT2 = em.find(Address.class, address.getContinuityId());
        Person pT2 = em.newEntity(Person.class);
        pT2.setAddress(aT2);
        em.getTransaction().commit();
        em.close();

        em = getEntityManager();
        em.setEffectiveTime(T2);

        em.getTransaction().begin();
        em.getEditionSet();
        try {
            EditionSetHelper.move(em, T5);
            em.getTransaction().commit();
        } catch (IllegalStateException e) {
            return;
        }
        Assert.fail("IllegalStateException execpted for violating FK");
    }

    /**
     * Create a new Address Entity (BOT) and then reference an edition of it
     * from a future entity at T2. In a subsequent transaction modify the
     * Address at BOT to have an end of T1. This would cause the FK relationship
     * from Person at T2 to be invalid.
     */
    @Test
    public void breakFKByChangingEditionEffectiveEnd() {
        TemporalEntityManager em = getEntityManager();
        em.getTransaction().begin();
        Address address = em.newEntity(Address.class);
        em.getTransaction().commit();
        em.close();

        em = getEntityManager();
        em.setEffectiveTime(T2);

        em.getTransaction().begin();
        Address aBOT = em.find(Address.class, address.getContinuityId());
        Address aT2 = em.newEdition(aBOT);
        Person pT2 = em.newEntity(Person.class);
        pT2.setAddress(aT2);
        em.getTransaction().commit();
        em.close();

        em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        pT2 = em.find(Person.class, pT2.getContinuityId());
        em.newEdition(pT2);
        em.getTransaction().commit();

        em = getEntityManager();
        em.setEffectiveTime(T2);

        em.getTransaction().begin();
        aT2 = em.find(Address.class, address.getContinuityId());
        aT2.getEffectivity().setEnd(T3);
        try {
            em.getTransaction().commit();
        } catch (IllegalStateException e) {
            return;
        }
        Assert.fail("IllegalStateException execpted for violating FK");
    }

    /**
     * Create a new Address Entity (BOT) and then reference it from a future
     * entity at T2. In a subsequent transaction modify the Address at BOT to
     * have an end of T1. This would cause the FK relationship from Person at T2
     * to be invalid.
     */
    @Test
    public void breakFKByChangingCurrentEffectiveEnd() {
        TemporalEntityManager em = getEntityManager();
        em.getTransaction().begin();
        Address address = em.newEntity(Address.class);
        em.getTransaction().commit();
        em.close();

        em = getEntityManager();
        em.setEffectiveTime(T2);

        em.getTransaction().begin();
        Address aT2 = em.find(Address.class, address.getContinuityId());
        Person pT2 = em.newEntity(Person.class);
        pT2.setAddress(aT2);
        em.getTransaction().commit();
        em.close();

        em = getEntityManager();
        Address aBOT = em.find(Address.class, address.getContinuityId());
        em.getTransaction().begin();
        aBOT.getEffectivity().setEnd(T1);
        em.getEditionSet();

        try {
            em.getTransaction().commit();
        } catch (IllegalStateException e) {
            return;
        }
        Assert.fail("IllegalStateException execpted for violating FK");
    }
}
