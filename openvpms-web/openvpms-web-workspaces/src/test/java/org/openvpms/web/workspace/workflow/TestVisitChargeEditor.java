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

package org.openvpms.web.workspace.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.customer.charge.EditorQueue;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;

/**
 * A test {@link VisitChargeEditor}.
 *
 * @author Tim Anderson
 */
public class TestVisitChargeEditor extends VisitChargeEditor implements EditorQueueHandle {

    /**
     * The editor queue.
     */
    private EditorQueue queue;

    /**
     * Constructs a {@code TestVisitChargeEditor}.
     *
     * @param queue   the editor queue
     * @param charge  the charge to edit
     * @param event   the visit to edit
     * @param context the layout context
     */
    public TestVisitChargeEditor(EditorQueue queue, FinancialAct charge, Act event, LayoutContext context) {
        super(charge, event, context, false); // don't add a default item...
        this.queue = queue;
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    public TestVisitChargeItemRelationshipCollectionEditor getItems() {
        return (TestVisitChargeItemRelationshipCollectionEditor) super.getItems();
    }

    /**
     * Returns the popup dialog manager.
     *
     * @return the popup dialog manager
     */
    @Override
    public EditorQueue getQueue() {
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
        TestVisitChargeItemRelationshipCollectionEditor result
                = new TestVisitChargeItemRelationshipCollectionEditor(items, act, getLayoutContext());
        result.setEditorQueue(new DelegatingEditorQueue(this));
        return result;
    }

}
