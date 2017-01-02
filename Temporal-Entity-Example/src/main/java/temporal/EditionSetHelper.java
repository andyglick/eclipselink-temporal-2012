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
package temporal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.Vector;

import javax.persistence.EntityManager;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

import temporal.persistence.DescriptorHelper;

/**
 * Apply an {@link EditionSet} making all of its contained editions the current.
 * This involves copying all relevant values into the current including the
 * {@link Effectivity} and then delete this edition. The OID of the current
 * should be changed to that of the edition when the operation is complete.
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
public class EditionSetHelper {

    /**
     * Apply the {@link EditionSet} causing all its editions to become the
     * continuity.
     * 
     * @param em
     * @param editionSet
     */
    public static void apply(EntityManager em, EditionSet editionSet) {
        for (EditionSetEntry ese : editionSet.getEntries()) {
            copyValues(em, ese);
        }
        em.remove(editionSet);
    }

    public static void copyValues(EntityManager em, EditionSetEntry entry) {
        TemporalEntity<?> edition = entry.getTemporalEntity();
        TemporalEntity<?> continuity = entry.getTemporalEntity().getContinuity();

        AbstractSession session = em.unwrap(RepeatableWriteUnitOfWork.class);
        ClassDescriptor descriptor = DescriptorHelper.getCurrentDescriptor(session, edition.getClass());

        for (String attr : entry.getAttributes()) {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(attr);

            if (!mapping.isForeignReferenceMapping()) {
                Object value = mapping.getRealAttributeValueFromObject(edition, session);
                Object oldValue = mapping.getRealAttributeValueFromObject(continuity, session);
                mapping.setRealAttributeValueInObject(continuity, value);
                if (continuity instanceof ChangeTracker) {
                    PropertyChangeListener listener = ((ChangeTracker) continuity)._persistence_getPropertyChangeListener();
                    listener.propertyChange(new PropertyChangeEvent(continuity, attr, oldValue, value));
                }
            }
        }

        // continuity.applyEdition(edition);

        continuity.getEffectivity().setEnd(edition.getEffectivity().getEnd());
    }

    /**
     * Move the provided {@link EditionSet} to the new effective time. In
     * addition to updating the effective start time for all editions within the
     * set the move must also handle:
     * <ul>
     * <li>Correct any applied change propagation to future editions from the
     * {@link EditionSet} being moved as well as the surrounding ones at the
     * source and destination.
     * <li>Properly updating the start and end time effective time on editions
     * the surround the source and target of the move.
     * <li>Update all temporal relationships so that the managed entity is
     * correct. This includes populating 1:1 and M:1 to ensure they are valid
     * and not causing broken FKs.
     * </ul>
     * 
     * @throws IllegalArgumentException
     *             for invalid effective time
     * @throws IllegalStateException
     *             if the current {@link EditionSet} has unwritten changes.
     */
    public static void move(TemporalEntityManager em, long effective) {
        if (effective <= Effectivity.BOT) {
            throw new IllegalArgumentException("Invalid effective time for move: " + effective);
        }
        RepeatableWriteUnitOfWork uow = em.unwrap(RepeatableWriteUnitOfWork.class);
        // Reject moves when changes still pending to the current EditionSet
        if (uow.getUnitOfWorkChangeSet() != null &&  uow.getUnitOfWorkChangeSet().hasChanges()) {
            throw new IllegalStateException("Cannot move EditionSet with pending changes"); // TODO: Confirm
        }
        if (!em.hasEditionSet()) {
            em.setEffectiveTime(effective);
            return;
        }

        EditionSet es = em.getEditionSet();

        // Need to ensure temporal objects in the EditionSetEntry(s) are loaded
        // as well as the 1:1 related temporals from the edition before changing
        // the effective time.
        for (EditionSetEntry entry : es.getEntries()) {
            entry.getTemporal();
            ClassDescriptor descriptor = DescriptorHelper.getEditionDescriptor(uow, entry.getTemporal().getClass());
            Set<OneToOneMapping> mappings = DescriptorHelper.getTemporalMappings(descriptor);
            for (OneToOneMapping mapping : mappings) {
                mapping.getRealAttributeValueFromObject(entry.getTemporal(), uow);
            }
        }        


        EditionSet newES = new EditionSet(effective);
        em.persist(newES);
        em.setEditionSet(newES);
        //em.clear();

        for (EditionSetEntry entry : es.getEntries()) {
            // Look through relationship mappings for references to temporal
            // which do not exist at the new effective time
            ClassDescriptor descriptor = DescriptorHelper.getEditionDescriptor(uow, entry.getTemporal().getClass());
            Set<OneToOneMapping> mappings = DescriptorHelper.getTemporalMappings(descriptor);

            for (OneToOneMapping mapping : mappings) {
                Temporal currentTarget = (Temporal) mapping.getRealAttributeValueFromObject(entry.getTemporal(), uow);

                if (currentTarget != null) {

                    if (!currentTarget.getEffectivity().includes(effective)) {
                        
                        Object id = ((TemporalEntity<?>) currentTarget).getContinuityId();
                        if (id instanceof Object[] || id instanceof Vector) {
                            throw new RuntimeException("Composite Key not supported");
                        }
                        @SuppressWarnings("unchecked")
                        Temporal target = em.find(mapping.getReferenceClass(), id);
                        if (target == null) {
                            throw new IllegalStateException();
                        }
                        mapping.setRealAttributeValueInObject(entry.getTemporal(), target);
                    }
                }
            }

            entry.getTemporal().getEffectivity().setStart(effective);
            newES.getEntries().add(entry);
            entry.setEditionSet(newES);
        }
        // Clear the moved entries from the EditionSet being removed.
        es.getEntries().clear();
        em.remove(es);

    }

}
