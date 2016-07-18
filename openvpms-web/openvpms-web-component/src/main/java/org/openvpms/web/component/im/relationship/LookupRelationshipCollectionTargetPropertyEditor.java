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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link LookupRelationship}s where the targets are being added
 * and removed.
 *
 * @author Tim Anderson
 */
public class LookupRelationshipCollectionTargetPropertyEditor
        extends RelationshipCollectionTargetPropertyEditor<LookupRelationship> {

    /**
     * Constructs a {@link LookupRelationshipCollectionTargetPropertyEditor}.
     *
     * @param property the property to edit
     * @param parent   the parent object
     */
    public LookupRelationshipCollectionTargetPropertyEditor(CollectionProperty property, Lookup parent) {
        super(property, parent);
    }

    /**
     * Creates a relationship between two objects.
     *
     * @param source    the source object
     * @param target    the target object
     * @param shortName the relationship short name
     * @return the new relationship, or {@code null} if it couldn't be created
     */
    @Override
    protected LookupRelationship addRelationship(IMObject source, IMObject target, String shortName) {
        Lookup src = (Lookup) source;
        Lookup tgt = (Lookup) target;
        LookupRelationship relationship = (LookupRelationship) ServiceHelper.getArchetypeService().create(shortName);
        if (relationship != null) {
            relationship.setSource(source.getObjectReference());
            relationship.setTarget(tgt.getObjectReference());
            src.addLookupRelationship(relationship);
            tgt.addLookupRelationship(relationship);
        }
        return relationship;
    }

    /**
     * Removes a relationship.
     *
     * @param source       the source object to remove from
     * @param target       the target object to remove from
     * @param relationship the relationship to remove
     * @return {@code true} if the relationship was removed
     */
    protected boolean removeRelationship(IMObject source, IMObject target, LookupRelationship relationship) {
        Lookup tgt = (Lookup) target;
        tgt.removeLookupRelationship(relationship);

        // Remove the relationship from the lookup entity. This will generate events, so invoke last
        return getProperty().remove(relationship);
    }
}