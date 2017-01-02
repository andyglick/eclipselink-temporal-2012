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

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.CopyGroup;

import model.Person;
import model.Phone;
import temporal.TemporalEntityManager;

import javax.persistence.TypedQuery;

import static example.PersonModelExample.GOLF;
import static example.PersonModelExample.RUN;
import static example.PersonModelExample.SKI;
import static example.PersonModelExample.T2;
import static example.PersonModelExample.T4;
import static example.PersonModelExample.T5;

/**
 * This test case performs current and edition queries on a simple
 * Person-Address-Phones model both illustrating and verifying query operations.
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class FullPersonWithEditionsMods extends FullPersonWithEditions
{

  @Test
  public void deleteAllAtT5()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T5);

    Person p = em.find(Person.class, getSample().getId());

    em.getTransaction().begin();

    p.getEffectivity().setEnd(T5);
    p.getAddress().getEffectivity().setEnd(T5);
    for (Phone phone : p.getPhones().values())
    {
      phone.getEffectivity().setEnd(T5);
    }

    em.flush();

    // TODO - validation

    em.getTransaction().rollback();
    em.close();
  }

  @Test
  public void detachResultUsingCopyPolicy()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T2);

    TypedQuery<Person> query = em.createNamedQuery("PersonEdition.find", Person.class);
    query.setParameter("ID", getSample().getId());

    Person p = query.getSingleResult();

    System.out.println("ORIGINAL: " + p + " HASHCODE: " + System.identityHashCode(p));
    System.out.println("\t" + p.getAddress());

    CopyGroup cg = new CopyGroup();
    cg.cascadeAllParts();

    Person pCopy = (Person) JpaHelper.getEntityManager(em).copy(p, cg);
    System.out.println("COPY: " + pCopy + " HASHSCODE: " + System.identityHashCode(pCopy));
    System.out.println("\t" + pCopy.getAddress());
  }

  @Test
  public void modifyFutureEditionOfCurrentPersonAtT4()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T4);

    Person pEdition = em.createQuery("SELECT p From Person p WHERE p.id = " + getSample().getId(), Person.class).getSingleResult();

    System.out.println("QUERY EDITION @ T4: " + pEdition);

    Assert.assertNotNull("No Person Edition Found", pEdition);
    Assert.assertFalse(pEdition.getEffectivity().isCurrent());
    Assert.assertTrue(pEdition.getEffectivity().isFutureEdition());
    Assert.assertEquals(T4, pEdition.getEffectivity().getStart());
    Assert.assertNotSame(pEdition, pEdition.getContinuity());

    Assert.assertEquals(2, pEdition.getPersonHobbies().size());
    Assert.assertTrue(pEdition.getPersonHobbies().containsKey(SKI));
    Assert.assertTrue(pEdition.getPersonHobbies().containsKey(RUN));
    Assert.assertFalse(pEdition.getPersonHobbies().containsKey(GOLF));

    long currentVersion = pEdition.getVersion();

    em.getTransaction().begin();
    pEdition.setName(pEdition.getName().toUpperCase());
    em.flush();

    Assert.assertEquals(currentVersion + 1, pEdition.getVersion());

    em.getTransaction().rollback();
    em.close();
  }

  @Test
  public void modifyFutureEditionOfCurrentPersonAtT4UsingMerge()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T4);

    Person pEdition = em.createQuery("SELECT p From Person p WHERE p.id = " + getSample().getId(), Person.class).getSingleResult();

    System.out.println("QUERY EDITION @ T4: " + pEdition);

    // Create new unregistered hobby and add.

    Assert.assertNotNull("No Person Edition Found", pEdition);
    Assert.assertFalse(pEdition.getEffectivity().isCurrent());
    Assert.assertTrue(pEdition.getEffectivity().isFutureEdition());
    Assert.assertEquals(T4, pEdition.getEffectivity().getStart());
    Assert.assertNotSame(pEdition, pEdition.getContinuity());

    Assert.assertEquals(2, pEdition.getPersonHobbies().size());
    Assert.assertTrue(pEdition.getPersonHobbies().containsKey(SKI));
    Assert.assertTrue(pEdition.getPersonHobbies().containsKey(RUN));
    Assert.assertFalse(pEdition.getPersonHobbies().containsKey(GOLF));

    long currentVersion = pEdition.getVersion();

    em.getTransaction().begin();
    pEdition.setName(pEdition.getName().toUpperCase());
    em.flush();

    Assert.assertEquals(currentVersion + 1, pEdition.getVersion());

    em.getTransaction().rollback();
    em.close();
  }

  @Test
  public void changeEffectiveTimeWithNoChanges()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T4);
    em.getEditionSet();

    Person pEdition = em.createQuery("SELECT p From Person p WHERE p.id = " + getSample().getId(), Person.class).getSingleResult();

    System.out.println("QUERY EDITION @ T4: " + pEdition);

    // Create new unregistered hobby and add.

    Assert.assertNotNull("No Person Edition Found", pEdition);

    em.setEffectiveTime(T5);

    Assert.assertFalse(em.hasEditionSet());
    Assert.assertEquals(T5, (long) em.getEffectiveTime());
  }

  @Test
  public void changeEffectiveTimeWithChangesPending()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T4);
    em.getEditionSet();

    Person pEdition = em.createQuery("SELECT p From Person p WHERE p.id = " + getSample().getId(), Person.class).getSingleResult();

    System.out.println("QUERY EDITION @ T4: " + pEdition);

    // Create new unregistered hobby and add.

    Assert.assertNotNull("No Person Edition Found", pEdition);

    em.getTransaction().begin();
    pEdition.setEmail(pEdition.getName() + "@email.com");

    try
    {
      em.setEffectiveTime(T5);
    }
    catch (IllegalStateException e)
    {
      return;
    }
    Assert.fail("IllegalStateException expected");
  }
}
