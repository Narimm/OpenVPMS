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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link IMObjectRelationship}s.
 * <p/>
 * NOTE: this collection editor is dependent on the order of saves - the parent object must be saved after the related
 * objects. This is because the parent is populated with the relationships, but the related objects are only retrieved
 * and populated when saving. If the parent is saved first, the related objects end up containing the relationship,
 * triggering a StaleObjectStateException.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class RelationshipCollectionPropertyEditor
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

    private Set<IMObjectRelationship> added
            = new HashSet<IMObjectRelationship>();

    private Set<IMObjectRelationship> removed
            = new HashSet<IMObjectRelationship>();


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
        boolean result = super.add(object);
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        if (result) {
            RelationshipState state = factory.create(getParent(), relationship,
                                                     parentIsSource);
            states.put(relationship, state);
            added.add(relationship);
        }
        return result;
    }

    /**
     * Removes an object from the collection.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return <tt>true</tt> if the object was removed
     */
    @Override
    public boolean remove(IMObject object) {
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        states.remove(relationship);
        if (!relationship.isNew()) {
            removed.add(relationship);
        } else {
            added.remove(relationship);
        }
        return super.remove(relationship);
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

    /**
     * Adds an object to the set of objects to save when the collection is
     * saved.
     * <p/>
     * This implementation is a no-op as the saving of relationship collections
     * is handled by the parent object.
     *
     * @param object the edited object
     */
    @Override
    protected void addEdited(IMObject object) {
        // no-op
    }

    /**
     * Saves the collection.
     * <p/>
     * For each removed relationship, this implementation also removes it
     * from the related object.
     * For each added relationship, this implementation also adds it to the
     * related object.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = false;
        Map<IMObjectReference, IMObject> toSave
                = new HashMap<IMObjectReference, IMObject>();
        for (IMObjectRelationship r : removed) {
            IMObject object = getObject(r, toSave);
            if (object != null) {
                removeRelationship(object, r);
            }
        }
        for (IMObjectRelationship r : added) {
            IMObject object = getObject(r, toSave);
            if (object != null) {
                addRelationship(object, r);
            }
        }
        if (!toSave.isEmpty()) {
            if (SaveHelper.save(toSave.values())) {
                removed.clear();
                added.clear();
                saved = true;
            }
        } else {
            saved = true;
        }
        setSaved(saved);
        return saved;
    }

    /**
     * Adds a relationship to the related object.
     *
     * @param object       the related object
     * @param relationship the relationship to add
     */
    protected abstract void addRelationship(IMObject object,
                                            IMObjectRelationship relationship);

    /**
     * Removes a relationship from a related object.
     *
     * @param object       the related object
     * @param relationship the relationship to remove
     */
    protected abstract void removeRelationship(
            IMObject object, IMObjectRelationship relationship);

    /**
     * Helper to return the related object in a relationship, first consulting
     * the supplied cache.
     * <p/>
     * Objects are added to the cache.
     *
     * @param relationship the relationship
     * @param cache        the object cache, keyed on reference
     * @return the related object, or <tt>null</tt> if not found
     */
    private IMObject getObject(IMObjectRelationship relationship,
                               Map<IMObjectReference, IMObject> cache) {
        IMObjectReference ref = parentIsSource() ? relationship.getTarget() : relationship.getSource();
        IMObject object = cache.get(ref);
        if (object == null) {
            object = ServiceHelper.getArchetypeService().get(ref);
            if (object != null) {
                cache.put(ref, object);
            }
        }
        return object;
    }

}
