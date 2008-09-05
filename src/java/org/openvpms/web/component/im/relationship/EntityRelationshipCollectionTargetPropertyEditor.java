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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * A {@link CollectionPropertyEditor} for collections of
 * {@link EntityRelationship}s where the targets are being added and removed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionTargetPropertyEditor
        extends RelationshipCollectionTargetPropertyEditor {

    /**
     * Creates a new <tt>EntityRelationshipCollectionTargetPropertyEditor</tt>.
     *
     * @param property the property to edit
     * @param parent   the parent object
     */
    public EntityRelationshipCollectionTargetPropertyEditor(
            CollectionProperty property, Entity parent) {
        super(property, parent);
    }

    /**
     * Creates a relationship between two objects.
     *
     * @param source    the source object
     * @param target    the target object
     * @param shortName
     * @return the new relationship, or <tt>null</tt> if it couldn't be created
     * @throws ArchetypeServiceException for any error
     */
    protected IMObjectRelationship addRelationship(IMObject source,
                                                   IMObject target,
                                                   String shortName) {
        EntityBean bean = new EntityBean((Entity) source);
        return bean.addRelationship(shortName, (Entity) target);
    }

    /**
     * Removes a relationship.
     *
     * @param source       the source object to remove from
     * @param target       the target object to remove from
     * @param relationship the relationship to remove
     */
    protected void removeRelationship(IMObject source,
                                      IMObject target,
                                      IMObjectRelationship relationship) {
        Entity sourceEntity = (Entity) source;
        Entity targetEntity = (Entity) target;
        EntityRelationship rel = (EntityRelationship) relationship;
        sourceEntity.removeEntityRelationship(rel);
        targetEntity.removeEntityRelationship(rel);
    }
}
