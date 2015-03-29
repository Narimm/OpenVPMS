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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.customer.charge.ChargeEditorQueue;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.workflow.DelegatingEditorQueue;
import org.openvpms.web.workspace.workflow.EditorQueueHandle;

/**
 * Tests implementation of the {@link OTCChargeEditor}.
 *
 * @author Tim Anderson
 */
class TestOTCChargeEditor extends OTCChargeEditor implements EditorQueueHandle {

    private final ChargeEditorQueue queue;

    /**
     * Constructs a {@link TestOTCChargeEditor}.
     *
     * @param queue   the popup editor queue
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public TestOTCChargeEditor(ChargeEditorQueue queue, FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context, false);
        this.queue = queue;
    }

    /**
     * Returns the popup dialog manager.
     *
     * @return the popup dialog manager
     */
    @Override
    public ChargeEditorQueue getEditorQueue() {
        return queue;
    }

    /**
     * Creates a collection editor for the items collection.
     *
     * @param act   the act
     * @param items the items collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
        ChargeItemRelationshipCollectionEditor result
                = new ChargeItemRelationshipCollectionEditor(items, act, getLayoutContext());
        result.setEditorQueue(new DelegatingEditorQueue(this));
        return result;
    }
}
