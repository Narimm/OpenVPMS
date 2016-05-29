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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;

/**
 * A test {@link DefaultCustomerChargeActEditor}.
 *
 * @author Tim Anderson
 */
public class TestChargeEditor extends DefaultCustomerChargeActEditor {

    /**
     * The pharmacy order service.
     */
    private TestPharmacyOrderService pharmacyOrderService;

    /**
     * The laboratory order service.
     */
    private TestLaboratoryOrderService laboratoryOrderService;

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
        return getItems().getEditor(object);
    }

    /**
     * Returns the editor queue.
     *
     * @return the editor queue
     */
    public EditorQueue getQueue() {
        return getItems().getEditorQueue();
    }

    /**
     * Returns the test pharmacy order service.
     *
     * @return the test pharmacy order service
     */
    public TestPharmacyOrderService getPharmacyOrderService() {
        return pharmacyOrderService;
    }

    /**
     * Returns the test laboratory order service.
     *
     * @return the test laboratory order service
     */
    public TestLaboratoryOrderService getLaboratoryOrderService() {
        return laboratoryOrderService;
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    public TestChargeItemRelationshipCollectionEditor getItems() {
        return (TestChargeItemRelationshipCollectionEditor) super.getItems();
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
        return new TestChargeItemRelationshipCollectionEditor(items, act, getLayoutContext());
    }

    /**
     * Creates a new {@link OrderPlacer}.
     *
     * @param customer the customer
     * @param location the practice location
     * @param user     the user responsible for the orders
     * @return a new pharmacy order placer
     */
    @Override
    protected OrderPlacer createOrderPlacer(Party customer, Party location, User user) {
        pharmacyOrderService = new TestPharmacyOrderService();
        laboratoryOrderService = new TestLaboratoryOrderService();
        OrderServices services = new OrderServices(pharmacyOrderService, ServiceHelper.getBean(Pharmacies.class),
                                                   laboratoryOrderService, ServiceHelper.getBean(Laboratories.class),
                                                   ServiceHelper.getBean(PatientContextFactory.class),
                                                   ServiceHelper.getBean(PatientInformationService.class),
                                                   ServiceHelper.getBean(MedicalRecordRules.class));
        return new OrderPlacer(customer, location, user, getLayoutContext().getCache(), services);
    }
}
