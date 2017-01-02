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

import org.junit.Test;

import example.PersonModelExample;
import junit.framework.Assert;
import model.Address;
import model.Person;
import model.Phone;
import temporal.EditionSet;
import temporal.EditionSetEntry;
import temporal.EditionSetHelper;
import temporal.TemporalEntityManager;
import tests.BaseTestCase;

import static example.PersonModelExample.*;
import static example.PersonModelExample.GOLF;
import static example.PersonModelExample.T1;
import static example.PersonModelExample.T2;
import static temporal.Effectivity.*;

/**
 * Tests applying a simple (no conflict) {@link EditionSet}
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class MoveSingleEditionSetTests extends BaseTestCase
{

  private static PersonModelExample example = new PersonModelExample();

  private Person getSample()
  {
    return example.fullPerson;
  }

  @Test
  public void moveEditionSetFromT2toT3()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T2);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);
    Assert.assertEquals(T2, es.getEffective());
    Assert.assertEquals(3, es.getEntries().size());

    for (EditionSetEntry entry : es.getEntries())
    {
      Assert.assertEquals(T2, entry.getTemporal().getEffectivity().getStart());
    }

    // Make the move
    em.getTransaction().begin();

    EditionSetHelper.move(em, T3);
    EditionSet newES = em.getEditionSet();

    Assert.assertEquals(T2, es.getEffective());
    Assert.assertEquals(0, es.getEntries().size());
    Assert.assertFalse(es.hasChanges());

    Assert.assertNotNull(newES);
    Assert.assertSame(newES, em.getEditionSet());
    Assert.assertEquals(T3, (long) em.getEffectiveTime());
    Assert.assertEquals(T3, newES.getEffective());
    Assert.assertEquals(3, newES.getEntries().size());
    Assert.assertTrue(newES.hasChanges());

    for (EditionSetEntry entry : newES.getEntries())
    {
      Assert.assertEquals(T3, entry.getTemporal().getEffectivity().getStart());
    }

    em.flush();

  }

  @Test
  public void moveEditionSetFromT2toT1()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T2);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);
    Assert.assertEquals(T2, es.getEffective());
    Assert.assertEquals(3, es.getEntries().size());

    for (EditionSetEntry entry : es.getEntries())
    {
      Assert.assertEquals(T2, entry.getTemporal().getEffectivity().getStart());
    }

    // Make the move
    em.getTransaction().begin();

    EditionSetHelper.move(em, T1);
    EditionSet newES = em.getEditionSet();

    Assert.assertEquals(T2, es.getEffective());
    Assert.assertEquals(0, es.getEntries().size());
    Assert.assertFalse(es.hasChanges());

    Assert.assertNotNull(newES);
    Assert.assertSame(newES, em.getEditionSet());
    Assert.assertEquals(T1, (long) em.getEffectiveTime());
    Assert.assertEquals(T1, newES.getEffective());
    Assert.assertEquals(3, newES.getEntries().size());
    Assert.assertTrue(newES.hasChanges());

    for (EditionSetEntry entry : newES.getEntries())
    {
      Assert.assertEquals(T1, entry.getTemporal().getEffectivity().getStart());
    }

    em.flush();

  }

  @Test
  public void moveEditionSetFromT2toBOT()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T2);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);
    Assert.assertEquals(T2, es.getEffective());
    Assert.assertEquals(3, es.getEntries().size());

    for (EditionSetEntry entry : es.getEntries())
    {
      Assert.assertEquals(T2, entry.getTemporal().getEffectivity().getStart());
    }

    // Make the move
    em.getTransaction().begin();

    try
    {
      EditionSetHelper.move(em, BOT);
    }
    catch (IllegalArgumentException iae)
    {
      return;
    }
    Assert.fail("Expected IllegalArgumentException not thrown");
  }

  /**
   * Populate initial sample entity
   */
  @Override
  public void populate(TemporalEntityManager em)
  {
    System.out.println("\nEditionSetTests.populate:START");
    example.populateHobbies(em);
    em.persist(getSample());
    populateT2Editions(em);
    System.out.println("\nEditionSetTests.populate::DONE");
  }

  /**
   * Create the edition at T2 if it has not already been created
   */
  public Person populateT2Editions(TemporalEntityManager em)
  {
    em.setEffectiveTime(T2);
    EditionSet editionSet = em.getEditionSet();
    Assert.assertNotNull(editionSet);

    Person personEditionT2 = em.find(Person.class, getSample().getId());

    if (personEditionT2.getEffectivity().getStart() != T2)
    {
      System.out.println("\nEditionSetTests.populateT2Edition:START");

      editionSet.setDescription("EditionSetTests::Person@T2");
      personEditionT2 = em.newEdition(personEditionT2);
      personEditionT2.setName("Jimmy");
      Address aT2 = em.newEdition(personEditionT2.getAddress());
      aT2.setCity("Toronto");
      aT2.setState("ON");
      personEditionT2.setAddress(aT2);
      Phone pT2 = em.newEdition(personEditionT2.getPhone("Home"));
      personEditionT2.addPhone(pT2);
      pT2.setNumber("222-222-2222");
      em.persist(personEditionT2.addHobby(example.hobbies.get(GOLF), T2));
      em.flush();

      System.out.println("\nEditionSetTests.populateT2Edition::DONE");
    }

    return personEditionT2;
  }

}
