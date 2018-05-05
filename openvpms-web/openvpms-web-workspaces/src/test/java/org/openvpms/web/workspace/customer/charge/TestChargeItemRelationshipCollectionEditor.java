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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * Test {@link ChargeItemRelationshipCollectionEditor}.
 *
 * @author Tim Anderson
 */
public class TestChargeItemRelationshipCollectionEditor extends ChargeItemRelationshipCollectionEditor {

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public TestChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
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

    /**
     * Sets the current editor.
     * <p/>
     * This registers a listener so that {@link #onCurrentEditorModified()} is invoked when the editor changes.
     * If there is an existing editor, its listener is removed.
     *
     * @param editor the editor. May be {@code null}
     */
    @Override
    public void setCurrentEditor(IMObjectEditor editor) {
        super.setCurrentEditor(editor);
    }
}
