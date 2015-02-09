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

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.Quantity;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * Estimate item collection editor.
 *
 * @author Tim Anderson
 */
public class EstimateActRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * Constructs an {@link EstimateActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public EstimateActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Sets the quantity.
     *
     * @param editor   the editor
     * @param quantity the quantity
     */
    @Override
    protected void setQuantity(ActItemEditor editor, Quantity quantity) {
        EstimateItemEditor itemEditor = (EstimateItemEditor) editor;
        itemEditor.setLowQuantity(quantity.getLowQuantity());
        itemEditor.setHighQuantity(quantity.getHighQuantity());
    }
}
