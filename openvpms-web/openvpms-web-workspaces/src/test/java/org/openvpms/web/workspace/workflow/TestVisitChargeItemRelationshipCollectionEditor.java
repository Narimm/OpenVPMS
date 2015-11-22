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

package org.openvpms.web.workspace.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.patient.charge.VisitChargeItemRelationshipCollectionEditor;

/**
 * Tests {@link VisitChargeItemRelationshipCollectionEditor}.
 *
 * @author Tim Anderson
 */
public class TestVisitChargeItemRelationshipCollectionEditor extends VisitChargeItemRelationshipCollectionEditor {

    /**
     * Constructs a {@link TestVisitChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public TestVisitChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act,
                                                           LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in an
     * editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    @Override
    public IMObjectEditor onAdd() {
        return super.onAdd();
    }

}
