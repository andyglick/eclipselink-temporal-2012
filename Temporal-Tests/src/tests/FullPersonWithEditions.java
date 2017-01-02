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
package tests;

import static example.PersonModelExample.GOLF;
import static example.PersonModelExample.RUN;
import static example.PersonModelExample.SKI;
import static example.PersonModelExample.T2;
import static example.PersonModelExample.T4;

import java.sql.Date;

import javax.persistence.EntityManagerFactory;

import junit.framework.Assert;
import model.Address;
import model.Person;
import model.Phone;
import model.entities.PhoneEntity;
import temporal.Effectivity;
import temporal.TemporalEntityManager;
import example.PersonModelExample;

/**
 * This test case performs current and edition queries on a simple
 * Person-Address-Phones model both illustrating and verifying query operations.
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public abstract class FullPersonWithEditions extends BaseTestCase {

    private static PersonModelExample example = null;

    protected Person getSample() {
        return example.fullPerson;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void populate(EntityManagerFactory emf) {
        System.out.println("\nFullPersonWithEditions.populate:START");
        example = new PersonModelExample();

        TemporalEntityManager em = TemporalEntityManager.getInstance(emf.createEntityManager());
        em.getTransaction().begin();

        example.populateHobbies(em);
        em.persist(example.fullPerson);
        em.flush();

        System.out.println("\n> Create T2 Edition");
        em.setEffectiveTime(T2);

        Person fpEdition = em.find(Person.class, example.fullPerson.getId());

        Person personEditionT2 = em.newEdition(fpEdition);

        personEditionT2.setName("Jimmy");
        Address aT2 = em.newEdition(example.fullPerson.getAddress());
        aT2.setCity("Toronto");
        aT2.setState("ON");
        personEditionT2.setDateOfBirth(new Date(75, 1, 5));

        personEditionT2.setAddress(aT2);
        Phone pT2 = em.newEdition(example.fullPerson.getPhone("Home"));
        personEditionT2.addPhone(pT2);
        pT2.setNumber("222-222-2222");
        Phone pWT2 = em.newEntity(PhoneEntity.class);
        pWT2.setType("Work");
        pWT2.setNumber("333-333-3333");
        personEditionT2.addPhone(pWT2);

        em.persist(personEditionT2.addHobby(example.hobbies.get(GOLF), T2));

        em.flush();

        System.out.println("\n> Create T4 Edition");
        em.setEffectiveTime(T4);

        fpEdition = em.find(Person.class, example.fullPerson.getId());

        Person personEditionT4 = em.newEdition(personEditionT2);
        personEditionT4.setName("James");
        Address aT4 = em.newEdition(aT2);
        aT4.setCity("San Francisco");
        aT4.setState("CA");
        personEditionT4.setAddress(aT4);

        Phone pT4 = em.newEdition(pT2);
        pT4.setNumber("444-444-4444");
        personEditionT4.addPhone(pT4);
        pWT2.getEffectivity().setEnd(T4);
        Phone pCT4 = em.newEntity(PhoneEntity.class);
        pCT4.setType("Cell");
        pCT4.setNumber("555-555-55555");
        personEditionT4.addPhone(pCT4);

        personEditionT4.getPersonHobbies().get(GOLF).getEffectivity().setEnd(T4);

        em.persist(personEditionT4.addHobby(example.hobbies.get(RUN), T4));
        em.persist(personEditionT4.addHobby(example.hobbies.get(SKI), T4));

        em.flush();

        em.getTransaction().commit();
        em.close();

        verifyPopulate(emf);

        System.out.println("\nFullPersonWithEditions.populate::DONE");
    }

    protected void verifyPopulate(EntityManagerFactory emf) {
        TemporalEntityManager em = TemporalEntityManager.getInstance(emf.createEntityManager());
        verifyCurrent(em);
        em.close();

        em = TemporalEntityManager.getInstance(emf.createEntityManager());
        verifyT2(em);
        em.close();

        em = TemporalEntityManager.getInstance(emf.createEntityManager());
        verifyT4(em);
        em.close();
    }

    public void verifyCurrent(TemporalEntityManager em) {
        em.setEffectiveTime(Effectivity.BOT);

        Person person = em.find(Person.class, getSample().getContinuityId());

        Assert.assertNotNull(person);

        Address address = person.getAddress();
        Assert.assertNotNull(address);

        Assert.assertEquals(1, person.getPhones().size());

        Phone homePhone = person.getPhone("Home");
        Assert.assertNotNull(homePhone);
    }

    public void verifyT2(TemporalEntityManager em) {
        em.setEffectiveTime(T2);

        Person person = em.find(Person.class, getSample().getContinuityId());

        Assert.assertNotNull(person);

        Address address = person.getAddress();
        Assert.assertNotNull(address);

        Assert.assertEquals(2, person.getPhones().size());

        Phone homePhone = person.getPhone("Home");
        Assert.assertNotNull(homePhone);

        Phone workPhone = person.getPhone("Work");
        Assert.assertNotNull(workPhone);
    }

    public void verifyT4(TemporalEntityManager em) {
        em.setEffectiveTime(T4);

        Person person = em.find(Person.class, getSample().getContinuityId());

        Assert.assertNotNull(person);

        Address address = person.getAddress();
        Assert.assertNotNull(address);

        Assert.assertEquals(2, person.getPhones().size());

        Phone homePhone = person.getPhone("Home");
        Assert.assertNotNull(homePhone);

        Phone cellPhone = person.getPhone("Cell");
        Assert.assertNotNull(cellPhone);
    }
}
