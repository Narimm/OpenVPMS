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

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.customer.charge.AbstractChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.ChargeEditContext;

/**
 * Estimate item collection editor.
 *
 * @author Tim Anderson
 */
public class EstimateActRelationshipCollectionEditor extends AbstractChargeItemRelationshipCollectionEditor {

    /**
     * The edit context.
     */
    private ChargeEditContext editContext;

    /**
     * Constructs an {@link EstimateActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public EstimateActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
        editContext = new ChargeEditContext(context);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        return initialiseEditor(new EstimateItemEditor((Act) object, (Act) getObject(), getEditContext(),
                                                       context));
    }

    /**
     * Initialises an editor.
     *
     * @param editor the editor
     * @return the editor
     */
    protected IMObjectEditor initialiseEditor(EstimateItemEditor editor) {
        editor.setProductListener(getProductListener()); // register the listener to expand templates
        return editor;
    }

    /**
     * Returns the edit context.
     *
     * @return the edit context
     */
    protected ChargeEditContext getEditContext() {
        return editContext;
    }

}
