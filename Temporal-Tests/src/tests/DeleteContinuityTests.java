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
import static example.PersonModelExample.T2;
import static example.PersonModelExample.T4;
import model.Address;
import model.Person;
import model.Phone;

import org.junit.Assert;
import org.junit.Test;

import temporal.TemporalEntityManager;
import example.PersonModelExample;

/**
 * TODO
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class DeleteContinuityTests extends BaseTestCase {

    private static PersonModelExample example = new PersonModelExample();

    private Person getSample() {
        return example.fullPerson;
    }

    @Test
    public void test() {
        getSample();
        Assert.fail("NOT YET IMPLEMENTED");
    }

    @Override
    public void populate(TemporalEntityManager em) {
        System.out.println("\nFullPersonWithEditions.populate:START");

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
        personEditionT2.setAddress(aT2);
        Phone pT2 = em.newEdition(example.fullPerson.getPhone("Home"));
        personEditionT2.addPhone(pT2);
        pT2.setNumber("222-222-2222");
        em.persist(personEditionT2.addHobby(example.hobbies.get("golf"), T2));
        em.flush();

        System.out.println("\n> Create T4 Edition");
        em.setEffectiveTime(T4);

        Person personEditionT4 = em.newEdition(personEditionT2);
        personEditionT4.setName("James");
        Address aT4 = em.newEdition(aT2);
        aT4.setCity("San Francisco");
        aT4.setState("CA");
        personEditionT4.setAddress(aT4);
        Phone pT4 = em.newEdition(pT2);
        pT4.setNumber("444-444-4444");
        personEditionT4.addPhone(pT4);
        personEditionT4.removeHobby(example.hobbies.get(GOLF), T4, T4);
        personEditionT4.addHobby(example.hobbies.get("running"), T4);
        personEditionT4.addHobby(example.hobbies.get("skiing"), T4);
        em.flush();

        System.out.println("\nFullPersonWithEditions.populate::DONE");
    }

}
