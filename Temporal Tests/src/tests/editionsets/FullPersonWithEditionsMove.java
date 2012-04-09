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
import junit.framework.Assert;
import model.Person;

import org.junit.Test;

import temporal.EditionSetHelper;
import temporal.Effectivity;
import temporal.TemporalEntityManager;
import tests.FullPersonWithEditions;

/**
 * Tests change propagation through future editions.
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class FullPersonWithEditionsMove extends FullPersonWithEditions {

    @Test
    public void moveT2toT3() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T3);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT2toT1() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T1);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT2toBOT() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        try {
            EditionSetHelper.move(em, Effectivity.BOT);
        } catch (IllegalArgumentException e) {
            return;
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
        Assert.fail("Expected IllegalArgumentException");
    }

    @Test
    public void moveT2toT4() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T4);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT2toT5() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T5);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT4toT3() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T3);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT4toT5() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T5);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT4toT2() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T2);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT4toT1() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        EditionSetHelper.move(em, T1);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void moveT4toBOT() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T4);
        em.getTransaction().begin();

        try {
            EditionSetHelper.move(em, Effectivity.BOT);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Expected IllegalArgumentException");
    }

    @Test
    public void moveT2WithPersonChangestoT3() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        Person person = em.find(Person.class, getSample().getContinuityId());
        person.setEmail("newemail@b.c");

        try {
            EditionSetHelper.move(em, T3);
        } catch (IllegalStateException e) {
            return;
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
        Assert.fail("IllegalStateException expected");
    }

    @Test
    public void moveT2WithAddressChangestoT3() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        Person person = em.find(Person.class, getSample().getContinuityId());
        person.getAddress().setCity("NEW CITY");

        try {
            EditionSetHelper.move(em, T3);
        } catch (IllegalStateException e) {
            return;
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
        Assert.fail("IllegalStateException expected");
    }

    @Test
    public void moveT2WithPhoneChangestoT3() {
        TemporalEntityManager em = getEntityManager();
        em.setEffectiveTime(T2);
        em.getTransaction().begin();

        Person person = em.find(Person.class, getSample().getContinuityId());
        person.getPhone("Home").setNumber("NEW NUMBER");

        try {
            EditionSetHelper.move(em, T3);
        } catch (IllegalStateException e) {
            return;
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
        Assert.fail("IllegalStateException expected");
    }
}
