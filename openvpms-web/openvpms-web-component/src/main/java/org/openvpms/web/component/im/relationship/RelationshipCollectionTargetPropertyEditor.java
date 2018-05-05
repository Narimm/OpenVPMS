/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link IMObjectRelationship}s where the targets are being added
 * and removed.
 *
 * @author Tim Anderson
 */
public abstract class RelationshipCollectionTargetPropertyEditor<R extends IMObjectRelationship>
        extends AbstractCollectionPropertyEditor {

    /**
     * The parent object.
     */
    private final IMObject parent;

    /**
     * The set of targets being edited, and their associated relationships.
     */
    private Map<IMObject, R> targets;

    /**
     * The relationship archetype short names.
     */
    private final String[] relationshipShortNames;

    /**
     * The selected relationship type.
     */
    private String relationshipShortName;

    /**
     * The set of removed objects.
     */
    private final Set<IMObject> removed = new HashSet<>();

    /**
     * The set of removed editors.
     */
    private final Map<IMObject, IMObjectEditor> removedEditors = new HashMap<>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(RelationshipCollectionTargetPropertyEditor.class);


    /**
     * Constructs a {@link RelationshipCollectionTargetPropertyEditor}.
     *
     * @param property the property to edit
     * @param parent   the parent object
     */
    public RelationshipCollectionTargetPropertyEditor(CollectionProperty property, IMObject parent) {
        super(property);
        relationshipShortNames = property.getArchetypeRange();
        relationshipShortName = relationshipShortNames[0];
        this.parent = parent;
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
     * Returns the relationship archetype short names.
     *
     * @return the relationship short names
     */
    public String[] getRelationshipShortNames() {
        return relationshipShortNames;
    }

    /**
     * Returns the relationship archetype short name.
     *
     * @return the relationship short name
     */
    public String getRelationshipShortName() {
        return relationshipShortName;
    }

    /**
     * Sets the relationship archetype short name.
     * <p/>
     * Must be one of the archetypes returned by {@link #getRelationshipShortNames()}
     *
     * @param shortName the relationship short name
     */
    public void setRelationshipShortName(String shortName) {
        this.relationshipShortName = shortName;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    @Override
    public String[] getArchetypeRange() {
        return RelationshipHelper.getTargetShortNames(relationshipShortNames);
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     */
    @Override
    public boolean add(IMObject object) {
        boolean added = false;
        R relationship = getTargets().get(object);
        if (relationship == null) {
            try {
                relationship = addRelationship(parent, object, relationshipShortName);
                if (relationship != null) {
                    getTargets().put(object, relationship);
                    getProperty().add(relationship);
                    added = true;
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        addEdited(object);
        return added;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     * @return {@code true} if the object was removed
     */
    @Override
    public boolean remove(IMObject object) {
        boolean removed = queueRemove(object);
        R relationship = getTargets().remove(object);
        if (relationship != null) {
            removeRelationship(parent, object, relationship);
        }
        return removed;
    }

    /**
     * Returns the target objects in the collection.
     *
     * @return the target objects in the collection
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<IMObject> getObjects() {
        return new ArrayList<>(getTargets().keySet());
    }

    /**
     * Returns the relationship for a target object.
     *
     * @param target the target object
     * @return the relationship, or {@code null} if none is found
     */
    public R getRelationship(IMObject target) {
        return getTargets().get(target);
    }

    /**
     * Returns the relationships.
     *
     * @return the relationships
     */
    public List<R> getRelationships() {
        return new ArrayList<>(getTargets().values());
    }

    /**
     * Creates a relationship between two objects.
     *
     * @param source    the source object
     * @param target    the target object
     * @param shortName the relationship short name
     * @return the new relationship, or {@code null} if it couldn't be created
     * @throws ArchetypeServiceException for any error
     */
    protected abstract R addRelationship(IMObject source, IMObject target, String shortName);

    /**
     * Removes a relationship.
     * <p/>
     * Note that this should invoke {@link CollectionProperty#remove} method to remove the relationship
     * but must also perform removal on the target of the relationship.
     * <p/>
     * The former is required in order for the {@link #isModified()} state to be correctly determined.
     *
     * @param source       the source object to remove from
     * @param target       the target object to remove from
     * @param relationship the relationship to remove
     * @return {@code true} if the relationship was removed
     */
    protected abstract boolean removeRelationship(IMObject source, IMObject target, R relationship);

    /**
     * Saves the collection.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        if (!removed.isEmpty()) {
            remove();
        }
        super.doSave();
    }

    /**
     * Invoked by {@link #doSave()} to remove objects queued for removal.
     *
     * @throws OpenVPMSException if the save fails
     */
    protected void remove() {
        IMObject[] toRemove = removed.toArray(new IMObject[removed.size()]);
        RemoveHandler handler = getRemoveHandler();
        for (IMObject object : toRemove) {
            IMObjectEditor editor = removedEditors.get(object);
            if (editor != null) {
                if (handler != null) {
                    handler.remove(editor);
                } else {
                    editor.delete();
                }
            } else {
                if (handler != null) {
                    handler.remove(object);
                } else {
                    ServiceHelper.getArchetypeService().remove(object);
                }
            }
            removed.remove(object);
            removedEditors.remove(object);
        }
    }

    /**
     * Returns the target objects.
     *
     * @return the target objects
     */
    protected Map<IMObject, R> getTargets() {
        if (targets == null) {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            List<R> relationships = super.getObjects();
            targets = new LinkedHashMap<>();
            for (R relationship : relationships) {
                IMObject target = (relationship.getTarget() != null) ? service.get(relationship.getTarget()) : null;
                if (target != null) {
                    targets.put(target, relationship);
                } else {
                    log.warn("Target object=" + relationship.getTarget()
                             + " doesn't exist. Referred to by relationship=" + relationship);
                    getProperty().remove(relationship);
                }
            }
        }
        return targets;
    }

    /**
     * Flags an object for removal when the collection is saved.
     *
     * @param object the object to remove
     * @return {@code true} if the object was removed
     */
    protected boolean queueRemove(IMObject object) {
        removed.add(object);
        return removeEdited(object);
    }

    /**
     * Removes an object from the the set of objects to save.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return {@code true} if the the object was being edited
     */
    @Override
    protected boolean removeEdited(IMObject object) {
        IMObjectEditor editor = getEditor(object);
        if (editor != null) {
            // use the editor to delete the object on save
            removedEditors.put(object, editor);
        }
        return super.removeEdited(object);
    }
}
