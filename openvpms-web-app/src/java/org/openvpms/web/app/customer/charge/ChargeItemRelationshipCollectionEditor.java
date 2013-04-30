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

package org.openvpms.web.app.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.AltModelActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;

import java.util.Date;


/**
 * Editor for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sets a {@link EditorQueue} on {@link CustomerChargeActItemEditor} instances.
 *
 * @author Tim Anderson
 */
public class ChargeItemRelationshipCollectionEditor
    extends AltModelActRelationshipCollectionEditor {

    /**
     * Last Selected Item Date.
     */
    private Date lastItemDate = null;

    /**
     * The popup editor manager.
     */
    private EditorQueue editorQueue;


    /**
     * Constructs a {@code ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
        editorQueue = new DefaultEditorQueue(context.getContext());
    }

    /**
     * Sets the popup editor manager.
     *
     * @param manager the popup editor manager
     */
    public void setEditorQueue(EditorQueue manager) {
        editorQueue = manager;
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
        final IMObjectEditor editor = super.createEditor(object, context);
        initialiseEditor(editor);
        return editor;
    }

    /**
     * Initialises an editor.
     *
     * @param editor the editor
     */
    protected void initialiseEditor(final IMObjectEditor editor) {
        if (editor instanceof CustomerChargeActItemEditor) {
            ((CustomerChargeActItemEditor) editor).setEditorQueue(editorQueue);
        }

        // Set startTime to to last used value
        if (lastItemDate != null) {
            editor.getProperty("startTime").setValue(lastItemDate);
        }

        // add a listener to store the last used item starttime.
        ModifiableListener startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                lastItemDate = (Date) editor.getProperty("startTime").getValue();
            }
        };
        editor.getProperty("startTime").addModifiableListener(startTimeListener);
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        ResultSet<IMObject> set = super.createResultSet();
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime", false)});
        return set;
    }

}
