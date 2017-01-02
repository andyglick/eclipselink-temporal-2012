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

import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;

import example.PersonModelExample;
import junit.framework.Assert;
import model.Address;
import model.Person;
import model.PersonHobby;
import model.Phone;
import temporal.EditionSet;
import temporal.EditionSetEntry;
import temporal.TemporalEntityManager;
import temporal.TemporalHelper;
import tests.BaseTestCase;

import java.util.List;

import javax.persistence.Temporal;

import static example.PersonModelExample.GOLF;
import static example.PersonModelExample.RUN;
import static example.PersonModelExample.SKI;
import static example.PersonModelExample.T2;
import static example.PersonModelExample.T4;
import static example.PersonModelExample.T5;
import static temporal.Effectivity.BOT;
import static temporal.Effectivity.EOT;

/**
 * Tests verifying the {@link EditionSet} capabilities.
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class DeleteEditionSetTests extends BaseTestCase
{

  private static PersonModelExample example = new PersonModelExample();

  private Person getSample()
  {
    return example.fullPerson;
  }

  @Test
  public void verifyEditionSetAtT2()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T2);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);
    Assert.assertEquals(T2, es.getEffective());
    Assert.assertEquals(4, es.getEntries().size());

    Assert.assertTrue(es.getEntries().get(0).getTemporal() instanceof Person);
    Person p = (Person) es.getEntries().get(0).getTemporal();
    Assert.assertEquals(T2, p.getEffectivity().getStart());

    Assert.assertTrue(es.getEntries().get(1).getTemporal() instanceof Address);
    Address a = (Address) es.getEntries().get(1).getTemporal();
    Assert.assertEquals(T2, a.getEffectivity().getStart());

    Assert.assertTrue(es.getEntries().get(2).getTemporal() instanceof Phone);
    Phone phone = (Phone) es.getEntries().get(2).getTemporal();
    Assert.assertEquals(T2, phone.getEffectivity().getStart());

    Assert.assertTrue(es.getEntries().get(3).getTemporal() instanceof PersonHobby);
    PersonHobby ph = (PersonHobby) es.getEntries().get(3).getTemporal();
    Assert.assertEquals(T2, ph.getEffectivity().getStart());
    Assert.assertEquals(PersonModelExample.GOLF, ph.getName());
    Assert.assertEquals(PersonModelExample.GOLF, ph.getHobby().getName());

    // Assert.assertSame(p, ph.getPerson());

    Assert.assertEquals(1, p.getPersonHobbies().size());

  }

  @Test
  public void verifyEditionSetAtT4()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T4);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);
    Assert.assertEquals(T4, es.getEffective());
    Assert.assertEquals(5, es.getEntries().size());

    Assert.assertTrue(es.getEntries().get(0).getTemporal() instanceof Person);
    Person p = (Person) es.getEntries().get(0).getTemporal();
    Assert.assertEquals(T4, p.getEffectivity().getStart());

    Assert.assertTrue(es.getEntries().get(1).getTemporal() instanceof Address);
    Address a = (Address) es.getEntries().get(1).getTemporal();
    Assert.assertEquals(T4, a.getEffectivity().getStart());

    Assert.assertTrue(es.getEntries().get(2).getTemporal() instanceof Phone);
    Phone phone = (Phone) es.getEntries().get(2).getTemporal();
    Assert.assertEquals(T4, phone.getEffectivity().getStart());

    Assert.assertTrue(es.getEntries().get(3).getTemporal() instanceof PersonHobby);
    PersonHobby ph = (PersonHobby) es.getEntries().get(3).getTemporal();
    Assert.assertEquals(T4, ph.getEffectivity().getStart());
    Assert.assertEquals(PersonModelExample.RUN, ph.getName());
    Assert.assertEquals(PersonModelExample.RUN, ph.getHobby().getName());
    // Assert.assertSame(p, ph.getPerson());

    Assert.assertTrue(es.getEntries().get(4).getTemporal() instanceof PersonHobby);
    ph = (PersonHobby) es.getEntries().get(4).getTemporal();
    Assert.assertEquals(T4, ph.getEffectivity().getStart());
    Assert.assertEquals(PersonModelExample.SKI, ph.getName());
    Assert.assertEquals(PersonModelExample.SKI, ph.getHobby().getName());
    // Assert.assertSame(p, ph.getPerson());

    Assert.assertEquals(2, p.getPersonHobbies().size());

  }

  @Test
  public void verifyEditionSets()
  {
    TemporalEntityManager em = getEntityManager();

    List<EditionSet> editionSets = em.createQuery("SELECT e FROM EditionSet e ORDER BY e.effective", EditionSet.class).getResultList();

    Assert.assertNotNull(editionSets);
    Assert.assertEquals("Incorrect number of EditionSets found.", 2, editionSets.size());

    EditionSet t1 = editionSets.get(0);
    Assert.assertNotNull(t1);
    Assert.assertEquals(T2, t1.getEffective());

    EditionSet t2 = editionSets.get(1);
    Assert.assertNotNull(t2);
    Assert.assertEquals(T4, t2.getEffective());

  }

  /**
   * Verify that the addition of a {@link Temporal} value in a 1:M collection
   * causes an EditionSetEntry to be created.
   */
  @Test
  public void addHobbyAtT5WithInitializedEditionSet()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T5);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);

    Person person = em.find(Person.class, getSample().getId());
    Assert.assertNotNull(person);
    Assert.assertTrue(TemporalHelper.isTemporalEntity(person));
    Assert.assertEquals(T5, es.getEffective());

    PersonHobby runHobby = em.newTemporal(PersonHobby.class);
    runHobby.setHobby(example.hobbies.get(RUN));
    person.addHobby(runHobby);

    Assert.assertEquals(1, es.getEntries().size());

    EditionSetEntry entry = es.getEntries().get(0);

    Assert.assertTrue(entry.getTemporal() instanceof PersonHobby);
  }

  /**
   * TODO
   */
  @Test
  public void deleteT4()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T4);

    EditionSet esT4 = em.getEditionSet();

    Assert.assertNotNull(esT4);

    em.getTransaction().begin();
    em.remove(esT4);

    RepeatableWriteUnitOfWork uow = em.unwrap(RepeatableWriteUnitOfWork.class);
    Assert.assertFalse(uow.getDeletedObjects().isEmpty());

    esT4 = em.find(EditionSet.class, T4);
    Assert.assertNull(esT4);
  }

  /**
   * TODO
   */
  @Test
  public void deleteT2()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T2);

    EditionSet esT2 = em.getEditionSet();

    Assert.assertNotNull(esT2);

    em.getTransaction().begin();
    em.remove(esT2);

    esT2 = em.find(EditionSet.class, T2);
    Assert.assertNull(esT2);
  }

  /**
   * Create future edition of Address@T2 and delete it.
   */
  @Test
  public void deleteFutureEntity()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T2);

    em.getTransaction().begin();

    Address addr = em.newEntity(Address.class);

    Assert.assertNotNull(addr);
    Assert.assertEquals(T2, addr.getEffectivity().getStart());
    Assert.assertEquals(EOT, addr.getEffectivity().getEnd());
    Assert.assertSame(addr, addr.getContinuity());
    Assert.assertTrue(addr.getContinuityId() > 0);

    em.getTransaction().commit();
    em.close();

    em = getEntityManager();
    em.setEffectiveTime(T2);

    addr = em.find(Address.class, addr.getContinuityId());

    Assert.assertNotNull(addr);
    Assert.assertEquals(T2, addr.getEffectivity().getStart());
    Assert.assertEquals(EOT, addr.getEffectivity().getEnd());

    em.getTransaction().begin();
    em.remove(addr);
    em.getTransaction().commit();
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

    System.out.println("\nEditionSetTests.populateT2Edition:START");

    em.setEffectiveTime(T2);
    EditionSet editionSet = em.getEditionSet();
    editionSet.setDescription("EditionSetTests::Person@T2");

    Person person = em.find(Person.class, getSample().getId());
    Assert.assertEquals(BOT, person.getEffectivity().getStart());

    Person personEditionT2 = em.newEdition(person);
    personEditionT2.setName("Jimmy");
    Address aT2 = em.newEdition(personEditionT2.getAddress());
    aT2.setCity("Toronto");
    aT2.setState("ON");
    personEditionT2.setAddress(aT2);
    Phone pT2 = em.newEdition(personEditionT2.getPhone("Home"));
    personEditionT2.addPhone(pT2);
    pT2.setNumber("222-222-2222");

    PersonHobby golfHobby = em.newTemporal(PersonHobby.class);
    golfHobby.setHobby(example.hobbies.get(GOLF));
    personEditionT2.addHobby(golfHobby);

    em.flush();

    System.out.println("\nEditionSetTests.populateT2Edition::DONE");

    System.out.println("\nEditionSetTests.populateT4Edition:START");

    em.setEffectiveTime(T4);
    editionSet = em.getEditionSet();
    editionSet.setDescription("EditionSetTests::Person@T4");

    person = em.find(Person.class, getSample().getId());
    Assert.assertEquals(T2, person.getEffectivity().getStart());

    Person personEditionT4 = em.newEdition(person);
    personEditionT4.setName("James");
    Address aT4 = em.newEdition(personEditionT4.getAddress());
    aT4.setCity("San Francisco");
    aT4.setState("CA");
    personEditionT4.setAddress(aT4);
    Phone pT4 = em.newEdition(personEditionT4.getPhone("Home"));
    pT4.setNumber("444-444-4444");
    personEditionT4.addPhone(pT4);
    personEditionT4.getPersonHobbies().get(GOLF).getEffectivity().setEnd(T4);

    PersonHobby runHobby = em.newTemporal(PersonHobby.class);
    runHobby.setHobby(example.hobbies.get(RUN));
    personEditionT4.addHobby(runHobby);

    PersonHobby skiHobby = em.newTemporal(PersonHobby.class);
    skiHobby.setHobby(example.hobbies.get(SKI));
    personEditionT4.addHobby(skiHobby);

    em.flush();

    System.out.println("\nEditionSetTests.populateT4Edition:DONE");

    System.out.println("\nEditionSetTests.populate::DONE");
  }

}
