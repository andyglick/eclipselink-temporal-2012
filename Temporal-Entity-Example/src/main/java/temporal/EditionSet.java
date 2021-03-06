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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ChangeTracking;
import org.eclipse.persistence.annotations.ChangeTrackingType;
import org.eclipse.persistence.annotations.Customizer;

import temporal.persistence.EditionSetEventListener;

/**
 * An EditionSet represents a proposed set of future changes that should be
 * performed together at the same effective time. This is an optimisation in the
 * model to collect all changes for a future point and simplify committing them
 * all together as the current.
 * 
 * @author dclarke
 * @since EclipseLink 2.3.1
 */
@Entity
@Table(name = "TEDITIONSET")
@ChangeTracking(ChangeTrackingType.DEFERRED)
@Customizer(EditionSetEventListener.class)
public class EditionSet {

    @Id
    @Column(name = "ID")
    private long effective;

    private String description;

    @OneToMany(mappedBy = "editionSet", cascade = CascadeType.ALL)
    private List<EditionSetEntry> entries = new ArrayList<EditionSetEntry>();

    private EditionSet() {
        super();
    }

    public EditionSet(long effective) {
        this();
        this.effective = effective;
    }

    public long getEffective() {
        return effective;
    }

    protected void setEffective(long effective) {
        this.effective = effective;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EditionSetEntry> getEntries() {
        return entries;
    }

    public EditionSetEntry add(Temporal temporalObject, boolean newEntity) {
        EditionSetEntry entry = new EditionSetEntry(this, temporalObject, newEntity);
        getEntries().add(entry);
        return entry;
    }

    public boolean hasChanges() {
        for (EditionSetEntry ese : getEntries()) {
            if (ese.hasChanges()) {
                return true;
            }
        }
        return false;
    }

    protected EditionSetEntry get(Temporal temporal) {
        for (EditionSetEntry ese : getEntries()) {
            if (ese.getTemporal().equals(temporal)) {
                return ese;
            }
        }
        return null;
    }

    /**
     * Remove the provided entity and its associated {@link EditionSetEntry}.
     * TODO: Throw exception if no ESE found
     */
    protected EditionSetEntry remove(Temporal temporal) {
        EditionSetEntry ese = get(temporal);
        if (ese != null) {
            getEntries().remove(ese);
        }
        return ese;
    }

    @Override
    public String toString() {
        return "EditionSet(" + Effectivity.timeString(getEffective()) + ")";
    }
}
