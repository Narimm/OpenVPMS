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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * An editor for {@link LookupRelationship}s.
 *
 * @author Tim Anderson
 */
public class LookupRelationshipEditor extends AbstractRelationshipEditor {

    /**
     * Constructs an {@link LookupRelationshipEditor}.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public LookupRelationshipEditor(LookupRelationship relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
    }

}
