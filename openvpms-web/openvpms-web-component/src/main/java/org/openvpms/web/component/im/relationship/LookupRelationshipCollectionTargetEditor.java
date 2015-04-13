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
 *  $Id: $
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of {@link LookupRelationship}s with 0..N cardinality.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class LookupRelationshipCollectionTargetEditor extends MultipleRelationshipCollectionTargetEditor {

    /**
     * Constructs a <tt>LookRelationshipCollectionTargetEditor</tt>.
     *
     * @param property the collection property
     * @param lookup   the parent lookup
     * @param context  the layout context
     */
    public LookupRelationshipCollectionTargetEditor(CollectionProperty property, Lookup lookup, LayoutContext context) {
        super(new LookupRelationshipCollectionTargetPropertyEditor(property, lookup), lookup, context);
    }
}
