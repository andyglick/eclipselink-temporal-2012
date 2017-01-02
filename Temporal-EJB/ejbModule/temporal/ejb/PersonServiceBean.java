/*******************************************************************************
 * Copyright (c) 2011-2012 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *      dclarke - Bug 361016: Future Versions Examples
 ******************************************************************************/
package temporal.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.queries.DatabaseQuery;

import model.Person;
import temporal.TemporalEntityManager;
import temporal.TemporalHelper;
import example.PersonModelExample;

/**
 * Session Bean implementation class PersonServiceBean
 */
@Stateless
public class PersonServiceBean implements PersonService {

    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext(unitName = "example")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PersonServiceBean() {
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    public List<Person> getAllCurrent() {
        TemporalEntityManager tem = TemporalEntityManager.getInstance(getEntityManager());

        TypedQuery<Person> query = tem.createQuery("SELECT p FROM Person p", Person.class);
        if (TemporalHelper.isEditionClass(query.unwrap(DatabaseQuery.class).getReferenceClass())) {
            System.out.println("HOW?");
        }
        return query.getResultList();
    }

    public List<Person> getAllAtT2() {
        TemporalEntityManager tem = TemporalEntityManager.getInstance(getEntityManager());
        tem.setEffectiveTime(PersonModelExample.T2);

        return tem.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void create(long id, String name, long effective) {
        TemporalEntityManager tem = TemporalEntityManager.getInstance(getEntityManager());
        tem.setEffectiveTime(effective);
        
        Person effectivePerson = tem.find(Person.class, id);
        
        if (effectivePerson == null) {
            throw new IllegalArgumentException("No current person found for id: " + id);
        }
        if (effectivePerson.getEffectivity().getStart() < effective) {
            Person editionPerson = tem.newEdition(effectivePerson);
            editionPerson.setName(name);
        }
    }

    @Override
    public List<Person> getAllEditions() {
        TemporalEntityManager tem = TemporalEntityManager.getInstance(getEntityManager());

        return tem.createQuery("SELECT p FROM PersonEditionView p", Person.class).getResultList();
    }
}
