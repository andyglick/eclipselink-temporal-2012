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
package tests.internal;

import org.junit.Test;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.server.ClientSession;
import org.eclipse.persistence.sessions.server.Server;

import junit.framework.Assert;
import model.Person;
import model.PersonHobby;
import temporal.EditionSet;
import temporal.TemporalEntityManager;
import temporal.persistence.DescriptorHelper;
import tests.BaseTestCase;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import static example.PersonModelExample.T1;
import static example.PersonModelExample.T3;
import static example.PersonModelExample.T4;
import static example.PersonModelExample.T5;
import static example.PersonModelExample.T6;
import static example.PersonModelExample.T7;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static temporal.Effectivity.BOT;
import static temporal.Effectivity.EOT;

/**
 * Verify the {@link TemporalEntityManager} API
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class TemporalEntityManagerTests extends BaseTestCase
{

  private void verifySetStartTime(TemporalEntityManager em, Long value)
  {
    assertTrue(em.hasEffectiveTime());
    assertNotNull(em.getEffectiveTime());
    assertEquals(value, em.getEffectiveTime());

    RepeatableWriteUnitOfWork uow = em.unwrap(RepeatableWriteUnitOfWork.class);
    assertNotNull(uow);

    ClientSession clientSession = (ClientSession) uow.getParent();

    DatabaseSession session = em.unwrap(DatabaseSession.class);
    assertNotSame(clientSession, session);

    Server serverSession = em.unwrap(Server.class);
    assertNotSame(clientSession, serverSession);
    assertSame(session, serverSession);
  }

  @Test
  public void verifyGetInstance()
  {
    TemporalEntityManager em1 = getEntityManager();

    Assert.assertTrue(em1.getProperties().containsKey(TemporalEntityManager.TEMPORAL_EM_PROPERTY));
    Assert.assertFalse(em1.getProperties().containsKey(TemporalEntityManager.EFF_TS_PROPERTY));
    assertFalse(em1.hasEffectiveTime());
    assertNull(em1.getEffectiveTime());

    EntityManager wrappedEm = em1.unwrap(EntityManager.class);
    Assert.assertNotSame(em1, wrappedEm);

    TemporalEntityManager em2 = TemporalEntityManager.getInstance(wrappedEm);
    Assert.assertSame(em1, em2);

    TemporalEntityManager em3 = TemporalEntityManager.getInstance(wrappedEm.unwrap(UnitOfWork.class));
    Assert.assertSame(em1, em3);

    TemporalEntityManager em4 = TemporalEntityManager.getInstance(em1);
    Assert.assertSame(em1, em4);
  }

  @Test
  public void verifySetStartTime()
  {
    TemporalEntityManager em = getEntityManager();

    assertFalse(em.hasEffectiveTime());
    assertNull(em.getEffectiveTime());

    em.setEffectiveTime(T1);

    verifySetStartTime(em, T1);
  }

  @Test
  public void verifyClearStartTime()
  {
    TemporalEntityManager em = getEntityManager();
    assertFalse(em.hasEffectiveTime());
    assertNull(em.getEffectiveTime());

    em.setEffectiveTime(T3);

    verifySetStartTime(em, T3);

    em.clear();

    assertFalse(em.hasEffectiveTime());
    assertNull(em.getEffectiveTime());
  }

  @Test
  public void verifyConcurrentSetStartTime()
  {
    TemporalEntityManager em1 = getEntityManager();
    TemporalEntityManager em2 = TemporalEntityManager.getInstance(getEMF().createEntityManager());

    assertNotSame(em1, em2);

    em1.setEffectiveTime(T4);

    em2.setEffectiveTime(T5);

    verifySetStartTime(em2, T5);
    verifySetStartTime(em1, T4);
  }

  @Test
  public void verifyConcurrentClearStartTime()
  {
    TemporalEntityManager em1 = getEntityManager();
    TemporalEntityManager em2 = TemporalEntityManager.getInstance(getEMF().createEntityManager());

    assertNotSame(em1, em2);

    em1.setEffectiveTime(T6);
    em2.setEffectiveTime(T7);

    verifySetStartTime(em2, T7);
    verifySetStartTime(em1, T6);

    em1.clear();
    verifySetStartTime(em2, T7);
    assertFalse(em1.hasEffectiveTime());
    assertNull(em1.getEffectiveTime());

    em2.clear();
    assertFalse(em2.hasEffectiveTime());
    assertNull(em2.getEffectiveTime());
  }

  @Test
  public void verifyCurrentCreateTemporal()
  {
    TemporalEntityManager em = getEntityManager();
    em.getTransaction().begin();

    PersonHobby ph = em.newTemporal(PersonHobby.class);

    Assert.assertNotNull(ph);
    Assert.assertNotNull(ph.getEffectivity());
    Assert.assertEquals(BOT, ph.getEffectivity().getStart());
    Assert.assertEquals(EOT, ph.getEffectivity().getEnd());
  }

  @Test
  public void verifyFutureCreateTemporal()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T3);
    em.getTransaction().begin();

    PersonHobby ph = em.newTemporal(PersonHobby.class);

    Assert.assertNotNull(ph);
    Assert.assertNotNull(ph.getEffectivity());
    Assert.assertEquals(T3, ph.getEffectivity().getStart());
    Assert.assertEquals(EOT, ph.getEffectivity().getEnd());
  }

  @Test
  public void verifyChangeEffectiveWithPendingChanges()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T3);
    EditionSet esT3 = em.getEditionSet();

    em.getTransaction().begin();

    Person newPersonAtT3 = em.newEntity(Person.class);
    Assert.assertNotNull(newPersonAtT3);
    Assert.assertEquals(T3, newPersonAtT3.getEffectivity().getStart());
    Assert.assertTrue(esT3.hasChanges());

    em.setEffectiveTime(T4);
    EditionSet esT4 = em.getEditionSet();

    Assert.assertFalse(esT4.hasChanges());
    Assert.assertTrue(esT3.hasChanges());

    em.flush();
  }

  @Test
  public void verifyRollbackWithPendingChanges()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T3);
    em.getTransaction().begin();

    Person newPersonAtT3 = em.newEntity(Person.class);
    Assert.assertNotNull(newPersonAtT3);
    Assert.assertEquals(T3, newPersonAtT3.getEffectivity().getStart());
    Assert.assertTrue(em.hasEditionSet());
    Assert.assertTrue(em.getEditionSet().hasChanges());

    // TODO : ?
    em.getTransaction().rollback();
  }

  /**
   * Verify proper query construction using interface alias. The concrete type
   * queried will depend on the state (effective time) of the
   * {@link TemporalEntityManager}
   */
  @Test
  public void createQuery_Person()
  {
    TemporalEntityManager em = getEntityManager();

    // Current Query
    TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p", Person.class);
    Assert.assertNotNull(query);
    ClassDescriptor desc = DescriptorHelper.getCurrentDescriptor(em.unwrap(Session.class), Person.class);
    Assert.assertSame(desc.getJavaClass(), query.unwrap(ObjectLevelReadQuery.class).getReferenceClass());
    Assert.assertSame(desc, query.unwrap(ObjectLevelReadQuery.class).getDescriptor());

    // Edition Query
    em.setEffectiveTime(T1);
    query = em.createQuery("SELECT p FROM Person p", Person.class);
    Assert.assertNotNull(query);
    desc = DescriptorHelper.getEditionDescriptor(em.unwrap(Session.class), Person.class);
    Assert.assertSame(desc.getJavaClass(), query.unwrap(ObjectLevelReadQuery.class).getReferenceClass());
    Assert.assertSame(desc, query.unwrap(ObjectLevelReadQuery.class).getDescriptor());

    // Current Query again
    em.clear();
    query = em.createQuery("SELECT p FROM Person p", Person.class);
    Assert.assertNotNull(query);
    desc = DescriptorHelper.getCurrentDescriptor(em.unwrap(Session.class), Person.class);
    Assert.assertSame(desc.getJavaClass(), query.unwrap(ObjectLevelReadQuery.class).getReferenceClass());
    Assert.assertSame(desc, query.unwrap(ObjectLevelReadQuery.class).getDescriptor());
  }

  @Test
  public void createQuery_PersonEdition()
  {
    TemporalEntityManager em = getEntityManager();

    // Current Query
    TypedQuery<Person> query = em.createQuery("SELECT p FROM PersonEdition p", Person.class);
    Assert.assertNotNull(query);
    ClassDescriptor desc = DescriptorHelper.getEditionDescriptor(em.unwrap(Session.class), Person.class);
    Assert.assertSame(desc.getJavaClass(), query.unwrap(ObjectLevelReadQuery.class).getReferenceClass());
    Assert.assertSame(desc, query.unwrap(ObjectLevelReadQuery.class).getDescriptor());
  }

  @Test
  public void createQuery_PersonEditionView()
  {
    TemporalEntityManager em = getEntityManager();

    // Current Query
    TypedQuery<Person> query = em.createQuery("SELECT p FROM PersonEditionView p", Person.class);
    Assert.assertNotNull(query);
    ClassDescriptor desc = DescriptorHelper.getEditionViewDescriptor(em.unwrap(Session.class), Person.class);
    Assert.assertSame(desc.getJavaClass(), query.unwrap(ObjectLevelReadQuery.class).getReferenceClass());
    Assert.assertSame(desc, query.unwrap(ObjectLevelReadQuery.class).getDescriptor());
  }

  /**
   * Ensure the find of a non-existent edition returns null and not the
   * {@link NoResultException} thrown by the .find named query execution.
   */
  @Test
  public void findNonExistent()
  {
    TemporalEntityManager em = getEntityManager();
    em.setEffectiveTime(T3);

    Person result = em.find(Person.class, 123456789l);
    Assert.assertNull(result);
  }
}
