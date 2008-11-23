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
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * A {@link CollectionPropertyEditor} for collections of
 * {@link LookupRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipCollectionPropertyEditor
        extends RelationshipCollectionPropertyEditor {

    /**
     * Creates a new <tt>LookupRelationshipCollectionPropertyEditor</tt>.
     *
     * @param property the collection property
     * @param parent   the parent object
     * @throws ArchetypeServiceException for any archetype service error
     */
    public LookupRelationshipCollectionPropertyEditor(
            CollectionProperty property, Lookup parent) {
        super(property, parent);
    }

    /**
     * Adds a relationship to the related object.
     *
     * @param object       the related object
     * @param relationship the relationship to add
     */
    protected void addRelationship(IMObject object,
                                   IMObjectRelationship relationship) {
        Lookup lookup = (Lookup) object;
        lookup.addLookupRelationship((LookupRelationship) relationship);
    }

    /**
     * Removes a relationship from a related object.
     *
     * @param object       the related object
     * @param relationship the relationship to remove
     */
    protected void removeRelationship(IMObject object,
                                      IMObjectRelationship relationship) {
        Lookup lookup = (Lookup) object;
        lookup.removeLookupRelationship((LookupRelationship) relationship);
    }

}
