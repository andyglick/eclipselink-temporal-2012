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

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.VariableOneToOneMapping;
import org.eclipse.persistence.sessions.server.Server;

import example.PersonModelExample;
import junit.framework.Assert;
import model.Person;
import temporal.EditionSet;
import temporal.TemporalEntityManager;
import temporal.TemporalHelper;
import tests.BaseTestCase;

import static example.PersonModelExample.T2;
import static example.PersonModelExample.T6;

/**
 * Tests verifying the {@link EditionSet} capabilities.
 *
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class EditionSetConfigTests extends BaseTestCase
{

  private static PersonModelExample example = new PersonModelExample();

  private Person getSample()
  {
    return example.fullPerson;
  }

  @Test
  public void verifyEditionSetMapping()
  {
    TemporalEntityManager em = getEntityManager();

    ClassDescriptor desc = em.unwrap(Server.class).getClassDescriptorForAlias("EditionSet");
    Assert.assertNotNull(desc);

    OneToManyMapping otmMapping = (OneToManyMapping) desc.getMappingForAttributeName("entries");
    Assert.assertNotNull(otmMapping);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void verifyEditionSetEntryMapping()
  {
    TemporalEntityManager em = getEntityManager();

    ClassDescriptor desc = em.unwrap(Server.class).getClassDescriptorForAlias("EditionSetEntry");
    Assert.assertNotNull(desc);

    VariableOneToOneMapping votoMapping = (VariableOneToOneMapping) desc.getMappingForAttributeName("temporal");
    Assert.assertNotNull(votoMapping);

    Assert.assertEquals(8, votoMapping.getTypeIndicatorTranslation().size());

    Assert.assertTrue(votoMapping.getTypeIndicatorTranslation().containsKey("Person"));
    Assert.assertTrue(TemporalHelper.isEditionClass((Class) votoMapping.getTypeIndicatorTranslation().get("Person")));

    Assert.assertTrue(votoMapping.getTypeIndicatorTranslation().containsKey("Phone"));
    Assert.assertTrue(TemporalHelper.isEditionClass((Class) votoMapping.getTypeIndicatorTranslation().get("Phone")));

    Assert.assertTrue(votoMapping.getTypeIndicatorTranslation().containsKey("Address"));
    Assert.assertTrue(TemporalHelper.isEditionClass((Class) votoMapping.getTypeIndicatorTranslation().get("Address")));

    Assert.assertTrue(votoMapping.getTypeIndicatorTranslation().containsKey("PersonHobby"));
    Assert.assertTrue(TemporalHelper.isTemporal((Class) votoMapping.getTypeIndicatorTranslation().get("PersonHobby"), false));
  }

  @Test
  public void verifySetEffectiveInitialize()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T6);
    EditionSet es = em.getEditionSet();

    Assert.assertNotNull(es);
    Assert.assertEquals(T6, es.getEffective());
  }

  @Test
  public void verifyEditionSetPerEffective()
  {
    TemporalEntityManager em = getEntityManager();

    em.setEffectiveTime(T2);
    EditionSet esT2 = em.getEditionSet();
    Assert.assertNotNull(esT2);
    Assert.assertEquals(T2, esT2.getEffective());

    em.setEffectiveTime(T6);
    EditionSet esT6 = em.getEditionSet();
    Assert.assertNotNull(esT6);
    Assert.assertEquals(T6, esT6.getEffective());

    Assert.assertNotSame(esT2, esT6);
  }

  @Test
  public void verifyNotInitialized()
  {
    TemporalEntityManager em = getEntityManager();
    Assert.assertFalse(em.hasEditionSet());

    em.setEffectiveTime(T6);

    Assert.assertFalse(em.hasEditionSet());
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
    System.out.println("\nEditionSetTests.populate::DONE");
  }

}
