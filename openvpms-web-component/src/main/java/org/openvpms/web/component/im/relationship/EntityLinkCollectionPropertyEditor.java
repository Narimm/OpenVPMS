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
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;


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
     * Adds a relationship to the related object.
     *
     * @param object the related object
     * @param link   the relationship to add
     */
    protected void addRelationship(IMObject object, IMObjectRelationship link) {
        Entity entity = (Entity) object;
        entity.addEntityLink((EntityLink) link);
    }

    /**
     * Removes a relationship from a related object.
     *
     * @param object the related object
     * @param link   the relationship to remove
     */
    protected void removeRelationship(IMObject object, IMObjectRelationship link) {
        Entity entity = (Entity) object;
        entity.removeEntityLink((EntityLink) link);
    }

}
