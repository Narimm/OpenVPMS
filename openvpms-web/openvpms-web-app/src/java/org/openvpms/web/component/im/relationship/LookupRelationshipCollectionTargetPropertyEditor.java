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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link LookupRelationship}s where the targets are being added
 * and removed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipCollectionTargetPropertyEditor
    extends RelationshipCollectionTargetPropertyEditor {

    /**
     * Constructs a <tt>LookupRelationshipCollectionTargetPropertyEditor</tt>.
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
     * @param shortName the relationship archetype short name
     * @return the new relationship, or <tt>null</tt> if it couldn't be created
     * @throws ArchetypeServiceException for any error
     */
    protected IMObjectRelationship addRelationship(IMObject source, IMObject target, String shortName) {
        Lookup src = (Lookup) source;
        Lookup tgt = (Lookup) target;
        LookupRelationship relationship = (LookupRelationship) ServiceHelper.getArchetypeService().create(shortName);
        if (relationship == null) {
            throw new IllegalArgumentException("No archetype for shortName: " + shortName);
        }
        relationship.setSource(src.getObjectReference());
        relationship.setTarget(tgt.getObjectReference());
        src.addLookupRelationship(relationship);
        tgt.addLookupRelationship(relationship);
        return relationship;
    }

    /**
     * Removes a relationship.
     *
     * @param source       the source object to remove from
     * @param target       the target object to remove from
     * @param relationship the relationship to remove
     */
    protected void removeRelationship(IMObject source, IMObject target, IMObjectRelationship relationship) {
        Lookup tgt = (Lookup) target;
        LookupRelationship rel = (LookupRelationship) relationship;
        tgt.removeLookupRelationship(rel);

        // Remove the relationship from the lookup entity. This will generate events, so invoke last
        getProperty().remove(relationship);
    }
}