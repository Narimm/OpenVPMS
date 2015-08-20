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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.openvpms.web.component.im.relationship.SequencedRelationshipCollectionHelper.sort;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link EntityLink}s where the targets are being added and
 * removed.
 *
 * @author Tim Anderson
 */
public class EntityLinkCollectionTargetPropertyEditor extends RelationshipCollectionTargetPropertyEditor {

    /**
     * Determines if the collection is sequenced.
     */
    private final boolean sequenced;

    /**
     * Constructs an {@link EntityLinkCollectionTargetPropertyEditor}.
     *
     * @param property the property to edit
     * @param parent   the parent object
     */
    public EntityLinkCollectionTargetPropertyEditor(CollectionProperty property, Entity parent) {
        super(property, parent);
        sequenced = SequencedRelationshipCollectionHelper.hasSequenceNode(property.getArchetypeRange());
        if (sequenced) {
            // make sure each relationship has a sequence number
            List<Map.Entry<IMObject, IMObjectRelationship>> entries = sort(getTargets());
            List<IMObject> relationships = new ArrayList<>();
            for (Map.Entry<IMObject, IMObjectRelationship> entry : entries) {
                relationships.add(entry.getValue());
            }
            SequencedRelationshipCollectionHelper.sequence(relationships);
        }
    }

    /**
     * Returns the target objects in the collection.
     *
     * @return the target objects in the collection
     */
    @Override
    public List<IMObject> getObjects() {
        List<IMObject> result;
        if (sequenced) {
            result = new ArrayList<>();
            List<Map.Entry<IMObject, IMObjectRelationship>> entries = sort(getTargets());
            for (Map.Entry<IMObject, IMObjectRelationship> entry : entries) {
                result.add(entry.getKey());
            }
        } else {
            result = super.getObjects();
        }
        return result;
    }

    /**
     * Creates a relationship between two objects.
     *
     * @param source    the source object
     * @param target    the target object
     * @param shortName the relationship archetype short name
     * @return the new relationship, or {@code null} if it couldn't be created
     * @throws ArchetypeServiceException for any error
     */
    @Override
    protected IMObjectRelationship addRelationship(IMObject source, IMObject target, String shortName) {
        EntityLink link = (EntityLink) IMObjectCreator.create(shortName);
        if (link != null) {
            link.setSource(source.getObjectReference());
            link.setTarget(target.getObjectReference());
            Entity entity = (Entity) source;
            entity.addEntityLink(link);

            if (sequenced) {
                int sequence = SequencedRelationshipCollectionHelper.getNextSequence(getTargets().values());
                link.setSequence(sequence);
            }
        }
        return link;
    }

    /**
     * Removes a relationship.
     *
     * @param source       the source object to remove from
     * @param target       the target object to remove from
     * @param relationship the relationship to remove
     * @return {@code true} if the relationship was removed
     */
    @Override
    protected boolean removeRelationship(IMObject source, IMObject target, IMObjectRelationship relationship) {
        return getProperty().remove(relationship);
    }

    /**
     * Invoked by {@link #doSave()} to remove objects queued for removal.
     * <p/>
     * For entity links, the parent must first be saved, otherwise constraint violations will occur.
     *
     * @return {@code true} if they were removed
     */
    @Override
    protected boolean remove() {
        return SaveHelper.save(getParent()) && super.remove();
    }
}
