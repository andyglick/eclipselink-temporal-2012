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
package model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import model.entities.PersonEntity;
import temporal.Effectivity;
import temporal.Temporal;

/**
 * Represents a Person's interest in a hobby. This relationship object is
 * {@link Temporal}, meaning that the time frame a person is interested in a
 * hobby is managed but there is not continuity for the relationship itself.
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
@Entity
@Table(name = "TPERSON_HOBBY")
public class PersonHobby implements Temporal {

    @Id
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hobby hobby;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = PersonEntity.class)
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "CID")
    private Person person;

    @Embedded
    private Effectivity effectivity = new Effectivity();

    public PersonHobby() {
        this.effectivity = new Effectivity();
    }

    public String getName() {
        return name;
    }

    public Hobby getHobby() {
        return hobby;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setHobby(Hobby hobby) {
        this.name = hobby.getName();
        this.hobby = hobby;
    }

    public Person getPerson() {
        return person;
    }

    public void setEffectivity(Effectivity effectivity) {
        this.effectivity = effectivity;
    }

    @Override
    public Effectivity getEffectivity() {
        return this.effectivity;
    }

    @Override
    public String toString() {
        return "PersonHobby[" + getPerson() + "->" + getName() + "]";
    }
}
