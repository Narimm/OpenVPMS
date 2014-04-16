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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Map;
import java.util.Set;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link EntityLink}s.
 *
 * @author Tim Anderson
 */
public class EntityLinkCollectionPropertyEditor extends RelationshipCollectionPropertyEditor {

    /**
     * Constructs an {@link EntityLinkCollectionPropertyEditor}.
     *
     * @param property the collection property
     * @param parent   the parent object
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityLinkCollectionPropertyEditor(CollectionProperty property, Entity parent) {
        super(property, parent);
    }

    /**
     * Invoked on save to add relationships to related objects.
     * <p/>
     * This implementation is a no-op, as {@link EntityLink}s are uni-directional.
     *
     * @param added   the added relationships
     * @param changed the changed objects, keyed on their references
     */
    @Override
    protected void addRelationships(Set<IMObjectRelationship> added, Map<IMObjectReference, IMObject> changed) {
    }

    /**
     * Invoked on save to remove relationships from related objects.
     * <p/>
     * This implementation is a no-op, as {@link EntityLink}s are uni-directional.
     *
     * @param removed the removed relationships
     * @param changed the changed objects, keyed on their references
     */
    @Override
    protected void removeRelationships(Set<IMObjectRelationship> removed, Map<IMObjectReference, IMObject> changed) {
    }

    /**
     * Adds a relationship to the related object.
     * <p/>
     * This implementation is a no-op, as {@link EntityLink}s are uni-directional.
     *
     * @param object the related object
     * @param link   the relationship to add
     */
    protected void addRelationship(IMObject object, IMObjectRelationship link) {
    }

    /**
     * Removes a relationship from a related object.
     * <p/>
     * This implementation is a no-op, as {@link EntityLink}s are uni-directional.
     *
     * @param object the related object
     * @param link   the relationship to remove
     */
    protected void removeRelationship(IMObject object, IMObjectRelationship link) {
    }

}
