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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * A {@link RelationshipDescriptorTableModel} that displays the target of a relationship.
 *
 * @author Tim Anderson
 */
public class TargetRelationshipDescriptorTableModel extends RelationshipDescriptorTableModel<IMObjectRelationship> {

    /**
     * Constructs a {@link TargetRelationshipDescriptorTableModel}.
     * <p/>
     * Enables selection if the context is in edit mode.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     */
    public TargetRelationshipDescriptorTableModel(String[] archetypes, LayoutContext context) {
        super(archetypes, context, true);
    }
}
