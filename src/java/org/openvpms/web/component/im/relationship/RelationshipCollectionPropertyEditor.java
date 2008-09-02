/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A {@link CollectionPropertyEditor} for collections of
 * {@link IMObjectRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipCollectionPropertyEditor
        extends AbstractCollectionPropertyEditor {

    /**
     * The parent object.
     */
    private final IMObject parent;

    /**
     * Determines if the parent is the source or target of the relationships.
     */
    private final boolean parentIsSource;

    /**
     * The relationship state factory.
     */
    private final RelationshipStateFactory factory;

    /**
     * Determines if inactive relationships should be excluded.
     */
    private boolean exclude = true;

    /**
     * The relationship states, keyed on their corresponding relationships.
     */
    private Map<IMObjectRelationship, RelationshipState> states
            = new LinkedHashMap<IMObjectRelationship, RelationshipState>();


    /**
     * Creates a new <tt>RelationshipCollectionPropertyEditor</tt>.
     *
     * @param property the collection property
     * @param parent   the parent object
     * @throws ArchetypeServiceException for any archetype service error
     */
    public RelationshipCollectionPropertyEditor(
            CollectionProperty property, IMObject parent) {
        super(property);
        this.parent = parent;

        RelationshipStateQuery query = createQuery(parent);
        parentIsSource = query.parentIsSource();
        factory = query.getFactory();
        states = query.query();
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public IMObject getParent() {
        return parent;
    }

    /**
     * Indicates if inactive relationships should be excluded.
     *
     * @param exclude if <tt>true</tt> exclude inactive relationships
     */
    public void setExcludeInactive(boolean exclude) {
        this.exclude = exclude;
    }

    /**
     * Determines if inactive relationships should be excluded.
     *
     * @return <tt>true</tt> if inactive relationships should be excluded
     */
    public boolean getExcludeInactive() {
        return exclude;
    }

    /**
     * Determines if the parent is the source or target of the relationships.
     *
     * @return <tt>true</tt> if the parent is the source, <tt>false</tt> if it
     *         is the target
     */
    public boolean parentIsSource() {
        return parentIsSource;
    }

    /**
     * Returns the relationship states, filtering inactive states if
     * {@link #getExcludeInactive()} is <tt>true</tt>.
     *
     * @return the relationship states
     */
    public Collection<RelationshipState> getRelationships() {
        Collection<RelationshipState> result;
        if (exclude) {
            result = new ArrayList<RelationshipState>();
            for (RelationshipState relationship : states.values()) {
                if (relationship.isActive()) {
                    result.add(relationship);
                }
            }
        } else {
            result = states.values();
        }
        return result;
    }

    /**
     * Returns the relationship state for a relationship.
     *
     * @param relationship the relationship
     * @return the corresponding state, or <tt>null</tt> if none is found
     */
    public RelationshipState getRelationshipState(
            IMObjectRelationship relationship) {
        return states.get(relationship);
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return <tt>true</tt> if the object was added, otherwise <tt>false</tt>
     */
    @Override
    public boolean add(IMObject object) {
        boolean added = super.add(object);
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        if (added) {
            RelationshipState state = factory.create(getParent(), relationship,
                                                     parentIsSource);
            states.put(relationship, state);
        }
        return added;
    }

    /**
     * Removes an object from the collection.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return <tt>true</tt> if the object was removed
     */
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean remove(IMObject object) {
        states.remove(object);
        return super.remove(object);
    }

    /**
     * Creates a new relationship state query.
     *
     * @param parent the parent entity
     * @return a new query
     */
    protected RelationshipStateQuery createQuery(IMObject parent) {
        return new RelationshipStateQuery(
                parent, getObjects(), getProperty().getArchetypeRange());
    }

}
