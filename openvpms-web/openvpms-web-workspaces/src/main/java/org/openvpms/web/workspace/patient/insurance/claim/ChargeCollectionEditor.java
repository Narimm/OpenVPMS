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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor for the collection of charges associated with a claim item.
 *
 * @author Tim Anderson
 * @see ClaimItemEditor
 */
public class ChargeCollectionEditor extends IMObjectTableCollectionEditor {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The charges.
     */
    private final Charges charges;

    /**
     * The attachments.
     */
    private final AttachmentCollectionEditor attachments;


    /**
     * Constructs a {@link ChargeCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param customer the customer
     * @param patient  the patient
     * @param charges  the charges
     * @param context  the layout context
     */
    public ChargeCollectionEditor(CollectionProperty property, Act object, Party customer, Party patient,
                                  Charges charges, AttachmentCollectionEditor attachments, LayoutContext context) {
        super(new ChargeRelationshipCollectionPropertyEditor(property, object, charges), object, context);
        this.customer = customer;
        this.patient = patient;
        this.charges = charges;
        this.attachments = attachments;
    }

    /**
     * Returns the charge acts.
     *
     * @return the charge acts
     */
    public List<Act> getActs() {
        return new ArrayList<>(getCollectionPropertyEditor().getActs().keySet());
    }

    /**
     * Determines if an invoice item can be claimed.
     *
     * @param item the invoice item
     * @return {@code true} if the item can be claimed, otherwise {@code false}
     */
    public boolean canClaim(Act item) {
        return charges.canClaimItem(item);
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    @Override
    protected ChargeRelationshipCollectionPropertyEditor getCollectionPropertyEditor() {
        return (ChargeRelationshipCollectionPropertyEditor) super.getCollectionPropertyEditor();
    }

    /**
     * Invoked when an object is selected in the table.
     */
    @Override
    protected void onSelected() {
        enableNavigation(getSelected() != null, true);
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     * @return the editor
     */
    @Override
    protected IMObjectEditor edit(IMObject object) {
        throw new IllegalStateException("Charges may not be edited");
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        throw new IllegalStateException("Charges may not be edited");
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in
     * an editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    @Override
    protected IMObjectEditor onAdd() {
        Act act = (Act) getObject();
        final ChargeBrowser browser = new ChargeBrowser(customer, patient, charges, act.getActivityStartTime(),
                                                        act.getActivityEndTime(), getContext());
        String title = Messages.format("imobject.select.title",
                                       DescriptorHelper.getDisplayName(CustomerAccountArchetypes.INVOICE));
        PopupDialog dialog = new PopupDialog(title, PopupDialog.OK_CANCEL) {
            {
                super.setStyleName("BrowserDialog");
                setModal(true);
                getLayout().add(browser.getComponent());
            }
        };
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                addSelections(browser);
            }
        });
        dialog.show();
        return null;
    }

    /**
     * Adds charges.
     *
     * @param browser the charge browser
     */
    protected void addSelections(ChargeBrowser browser) {
        List<Act> selected = browser.getSelectedItems();
        for (Act object : selected) {
            add(object);
        }
        refresh();
        for (FinancialAct invoice : browser.getSelectedInvoices()) {
            attachments.addInvoice(invoice);
        }
    }


    private static class ChargeRelationshipCollectionPropertyEditor extends ActRelationshipCollectionPropertyEditor {

        /**
         * The charges.
         */
        private final Charges charges;

        /**
         * Constructs an {@link ActRelationshipCollectionPropertyEditor}.
         *
         * @param property the property to edit
         * @param act      the parent act
         * @param charges  the charges
         */
        public ChargeRelationshipCollectionPropertyEditor(CollectionProperty property, Act act, Charges charges) {
            super(property, act);
            this.charges = charges;
            for (Act object : getActs().keySet()) {
                charges.add(object);
            }
        }

        /**
         * Adds an object to the collection, if it doesn't exist.
         *
         * @param object the object to add
         */
        @Override
        public boolean add(IMObject object) {
            boolean add = super.add(object);
            charges.add((Act) object);
            return add;
        }

        /**
         * Removes an object from the collection.
         *
         * @param object the object to remove
         * @return {@code true} if the object was removed
         */
        @Override
        public boolean remove(IMObject object) {
            boolean remove = super.remove(object);
            charges.remove((Act) object);
            return remove;
        }

        /**
         * Flags an object for removal when the collection is saved.
         *
         * @param object the object to remove
         * @return {@code true} if the object was removed
         */
        @Override
        protected boolean queueRemove(IMObject object) {
            return removeEdited(object);
        }
    }
}
