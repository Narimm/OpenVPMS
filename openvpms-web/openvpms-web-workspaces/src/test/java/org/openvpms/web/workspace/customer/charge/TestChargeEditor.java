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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * A test {@link CustomerChargeActEditor}.
 *
 * @author Tim Anderson
 */
public class TestChargeEditor extends CustomerChargeActEditor {

    /**
     * The editor queue.
     */
    private ChargeEditorQueue queue;

    /**
     * Constructs a {@link TestChargeEditor}.
     *
     * @param act            the act to edit
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     */
    public TestChargeEditor(FinancialAct act, LayoutContext context, boolean addDefaultItem) {
        super(act, null, context, addDefaultItem);
    }

    /**
     * Deletes an item.
     *
     * @param item the item to delete
     */
    public void delete(Act item) {
        getItems().remove(item);
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor. May be {@code null}
     */
    public CustomerChargeActItemEditor getCurrentEditor() {
        return (CustomerChargeActItemEditor) getItems().getCurrentEditor();
    }

    /**
     * Returns the editor associated with an object.
     *
     * @param object the object
     * @return the corresponding editor
     */
    public CustomerChargeActItemEditor getEditor(Act object) {
        return (CustomerChargeActItemEditor) getItems().getEditor(object);
    }

    /**
     * Returns the editor queue.
     *
     * @return the editor queue
     */
    public ChargeEditorQueue getQueue() {
        if (queue == null) {
            queue = new ChargeEditorQueue();
        }
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
        ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
        if (editor instanceof ChargeItemRelationshipCollectionEditor) {
            // register a handler for act popups
            ((ChargeItemRelationshipCollectionEditor) editor).setEditorQueue(getQueue());
        }
        return editor;
    }
}
