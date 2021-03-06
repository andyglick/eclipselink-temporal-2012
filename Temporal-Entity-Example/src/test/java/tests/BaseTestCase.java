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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

import temporal.TemporalEntityManager;
import temporal.persistence.TemporalSchemaManager;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * TODO
 *
 * @author dclarke
 * @Since EclipseLink 2.3.1
 */
public abstract class BaseTestCase
{

  private static EntityManagerFactory emf;

  private TemporalEntityManager entityManager;

  @Rule
  public TestName testName = new TestName();

  public EntityManagerFactory getEMF()
  {
    if (emf == null)
    {

      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(PersistenceUnitProperties.TRANSACTION_TYPE, "RESOURCE_LOCAL");
      properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, "");
      properties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
      properties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:test");
      properties.put(PersistenceUnitProperties.JDBC_USER, "sa");
      properties.put(PersistenceUnitProperties.JDBC_PASSWORD, "");

      emf = Persistence.createEntityManagerFactory("example", properties);

      Server session = JpaHelper.getServerSession(emf);

      SchemaManager sm = new TemporalSchemaManager(session);
      // Cache SchemaManager so it can be verified in test cases
      session.setProperty(SchemaManager.class.getName(), sm);
      sm.replaceDefaultTables(false, true);
      sm.replaceSequences();

      // Populate test case example instances
      populate(emf);

      System.out.println("\n--- CREATE EMF & POPULATE DONE ---\n");

      closeEntityManager();
    }
    return emf;
  }

  public TemporalEntityManager getEntityManager()
  {
    return getEntityManager(null);
  }

  public TemporalEntityManager getEntityManager(Long effectiveTime)
  {
    if (this.entityManager == null || !this.entityManager.isOpen())
    {
      TemporalEntityManager em = TemporalEntityManager.getInstance(getEMF().createEntityManager());
      em.setEffectiveTime(effectiveTime);
      this.entityManager = em;
    }
    return this.entityManager;
  }

  protected void populate(EntityManagerFactory emf)
  {
    TemporalEntityManager em = TemporalEntityManager.getInstance(emf.createEntityManager());
    em.getTransaction().begin();
    try
    {
      populate(em);
      em.getTransaction().commit();
    }
    finally
    {
      em.close();
    }
  }

  protected void populate(TemporalEntityManager em)
  {
  }

  @AfterClass
  @BeforeClass
  public static void closeEMF()
  {
    if (emf != null && emf.isOpen())
    {
      emf.close();
    }
    emf = null;
  }

  @Before
  public void logTestName()
  {
    System.out.println("\n\nTEST: " + testName.getMethodName() + "\n");
  }

  /**
   * After each test case ensure the {@link EntityManager} is closed and if a
   * transaction is active roll it back first.
   */
  @After
  public void closeEntityManager()
  {
    if (this.entityManager != null && this.entityManager.isOpen())
    {
      if (this.entityManager.getTransaction().isActive())
      {
        this.entityManager.getTransaction().rollback();
      }
      this.entityManager.close();
    }
    this.entityManager = null;
    if (emf != null && emf.isOpen())
    {
      JpaHelper.getServerSession(emf).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

  }

}
