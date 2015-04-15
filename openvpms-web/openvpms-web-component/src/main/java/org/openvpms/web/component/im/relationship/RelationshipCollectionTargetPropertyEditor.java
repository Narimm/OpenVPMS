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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A {@link CollectionPropertyEditor} for collections of
 * {@link IMObjectRelationship}s where the targets are being added and removed.
 *
 * @author Tim Anderson
 */
public abstract class RelationshipCollectionTargetPropertyEditor
        extends AbstractCollectionPropertyEditor {

    /**
     * The parent object.
     */
    private final IMObject parent;

    /**
     * The set of targets being edited, and their associated relationships.
     */
    private Map<IMObject, IMObjectRelationship> targets;

    /**
     * The relationship short name.
     */
    private final String relationshipType;

    /**
     * The set of removed objects.
     */
    private final Set<IMObject> removed = new HashSet<IMObject>();

    /**
     * The set of removed editors.
     */
    private final Map<IMObject, IMObjectEditor> removedEditors = new HashMap<IMObject, IMObjectEditor>();

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
        // @todo - no support for multiple relationship archetypes
        relationshipType = property.getArchetypeRange()[0];
        this.parent = parent;
    }

    /**
     * Returns the relationship archetype short name.
     *
     * @return the relationship short name
     */
    public String getRelationshipShortName() {
        return relationshipType;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    @Override
    public String[] getArchetypeRange() {
        ArchetypeDescriptor relationship = DescriptorHelper.getArchetypeDescriptor(relationshipType);
        NodeDescriptor target = relationship.getNodeDescriptor("target");
        return DescriptorHelper.getShortNames(target);
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     */
    @Override
    public boolean add(IMObject object) {
        boolean added = false;
        IMObjectRelationship relationship = getTargets().get(object);
        if (relationship == null) {
            try {
                relationship = addRelationship(parent, object, relationshipType);
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
        IMObjectRelationship relationship = getTargets().remove(object);
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
    @Override
    public List<IMObject> getObjects() {
        return new ArrayList<IMObject>(getTargets().keySet());
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
    protected abstract IMObjectRelationship addRelationship(IMObject source, IMObject target, String shortName);

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
    protected abstract boolean removeRelationship(IMObject source, IMObject target, IMObjectRelationship relationship);

    /**
     * Saves the collection.
     *
     * @return {@code true} if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = true;
        if (!removed.isEmpty()) {
            IMObject[] toRemove = removed.toArray(new IMObject[removed.size()]);
            boolean deleted;
            RemoveHandler handler = getRemoveHandler();
            for (IMObject object : toRemove) {
                IMObjectEditor editor = removedEditors.get(object);
                if (editor != null) {
                    if (handler != null) {
                        handler.remove(editor);
                        deleted = true;
                    } else {
                        deleted = editor.delete();
                    }
                } else {
                    if (handler != null) {
                        handler.remove(object);
                        deleted = true;
                    } else {
                        deleted = SaveHelper.delete(object, new DefaultIMObjectDeletionListener());
                    }
                }
                if (deleted) {
                    removed.remove(object);
                    removedEditors.remove(object);
                } else {
                    saved = false;
                    break;
                }
            }
            if (saved) {
                setSaved(true);
            }
        }
        if (saved) {
            saved = super.doSave();
        }
        return saved;
    }

    /**
     * Returns the target objects.
     *
     * @return the target objects
     */
    protected Map<IMObject, IMObjectRelationship> getTargets() {
        if (targets == null) {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> relationships = super.getObjects();
            targets = new LinkedHashMap<IMObject, IMObjectRelationship>();
            for (IMObject object : relationships) {
                IMObjectRelationship relationship = (IMObjectRelationship) object;
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
