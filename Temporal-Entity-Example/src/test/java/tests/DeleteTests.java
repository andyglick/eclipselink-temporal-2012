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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import example.PersonModelExample;
import model.Person;
import temporal.EditionWrapperHelper;
import temporal.TemporalEntityManager;

import javax.persistence.EntityManager;

import static example.PersonModelExample.T1;

/**
 * Tests to verify delete use cases. In this temporal usage delete is equivalent
 * to setting the end date to a point in time in the future. Then any queries
 * after that time result in no edition being returned.
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class DeleteTests extends BaseTestCase
{

  private static PersonModelExample examples = new PersonModelExample();

  @Test
  public void deleteCurrentSimple()
  {
    TemporalEntityManager em = getEntityManager();
    em.getTransaction().begin();
    em.persist(examples.simplePerson);
    em.getTransaction().commit();
    closeEntityManager();

    em = getEntityManager();

    Assert.assertEquals(1, em.createQuery("SELECT COUNT(p) FROM Person p", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(a) FROM Address a", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Phone p", Number.class).getSingleResult().intValue());
    Person p = em.createQuery("SELECT p FROM Person p", Person.class).getSingleResult();

    Assert.assertNotNull(p);

    em.getTransaction().begin();
    em.remove(p);
    em.getTransaction().commit();

    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Person p", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(a) FROM Address a", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Phone p", Number.class).getSingleResult().intValue());
  }

  @Test
  public void deleteCurrentSimpleAtT1()
  {
    TemporalEntityManager em = getEntityManager();
    em.getTransaction().begin();
    em.persist(examples.simplePerson);
    em.getTransaction().commit();
    closeEntityManager();

    em = getEntityManager();

    Assert.assertEquals(1, em.createQuery("SELECT COUNT(p) FROM Person p", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(a) FROM Address a", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Phone p", Number.class).getSingleResult().intValue());
    Person p = em.createQuery("SELECT p FROM Person p", Person.class).getSingleResult();

    Assert.assertNotNull(p);

    em.getTransaction().begin();

    Person pWrapper = EditionWrapperHelper.wrap(em, p);
    pWrapper.getEffectivity().setEnd(T1);

    em.getTransaction().commit();

    Assert.assertEquals(1, em.createQuery("SELECT COUNT(p) FROM Person p", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(a) FROM Address a", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Phone p", Number.class).getSingleResult().intValue());

    em.setEffectiveTime(T1);

    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Person p", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(a) FROM Address a", Number.class).getSingleResult().intValue());
    Assert.assertEquals(0, em.createQuery("SELECT COUNT(p) FROM Phone p", Number.class).getSingleResult().intValue());
  }

  @After
  public void deleteAll()
  {
    EntityManager em = TemporalEntityManager.getInstance(getEMF().createEntityManager());
    em.getTransaction().begin();
    em.createQuery("DELETE FROM PersonHobby ph").executeUpdate();
    em.createQuery("DELETE FROM Hobby h").executeUpdate();
    em.createNativeQuery("DELETE FROM TADDRESS").executeUpdate();
    em.createNativeQuery("DELETE FROM TPERSON_NNAMES").executeUpdate();
    em.createNativeQuery("UPDATE TPERSON SET CID = NULL").executeUpdate();
    em.createNativeQuery("DELETE FROM TPERSON").executeUpdate();
    em.createNativeQuery("DELETE FROM TPHONE").executeUpdate();
    em.getTransaction().commit();
    em.close();
  }
}
