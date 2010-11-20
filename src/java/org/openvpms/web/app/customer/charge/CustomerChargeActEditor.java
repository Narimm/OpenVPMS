/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.charge;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.FinancialActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>,
 * <em>act.customerAccountChargesCredit</em>
 * or <em>act.customerAccountChargesCounter</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerChargeActEditor extends FinancialActEditor {

    /**
     * Constructs a new <tt>CustomerChargeActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public CustomerChargeActEditor(FinancialAct act, IMObject parent,
                                   LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
    }

    /**
     * Updates the status.
     *
     * @param status the new status
     */
    public void setStatus(String status) {
        getProperty("status").setValue(status);
    }

    /**
     * Helper to ensure that clinical events exist for each referenced patient.
     * <p/>
     * This is a workaround for OVPMS-823.
     *
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    public void createClinicalEvents() {
        MedicalRecordRules rules = new MedicalRecordRules();
        ActRelationshipCollectionEditor editor = getEditor();
        ActBean bean = new ActBean((Act) getObject());
        Entity defaultClinician = bean.getNodeParticipant("clinician");
        if (defaultClinician == null) {
            defaultClinician = getLayoutContext().getContext().getClinician();
        }
        for (Act act : editor.getCurrentActs()) {
            ActBean item = new ActBean(act);
            Party patient = (Party) item.getNodeParticipant("patient");
            if (patient != null) {
                Entity clinician = item.getNodeParticipant("clinician");
                if (clinician == null) {
                    clinician = defaultClinician;
                }
                Date startTime = act.getActivityStartTime();
                Act event = rules.getEventForAddition(patient, startTime, clinician);
                if (event.isNew()) {
                    ServiceHelper.getArchetypeService().save(event);
                }
            }
        }
    }

    /**
     * Invoked when layout has completed.
     * <p/>
     * This invokes {@link #initItems()}.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        initItems();
    }

    /**
     * Updates the amount and tax when an act item changes.
     */
    @Override
    protected void onItemsChanged() {
        super.onItemsChanged();
        calculateCosts();
    }

    /**
     * Adds a default invoice item if there are no items present.
     */
    private void initItems() {
        ActRelationshipCollectionEditor items = getEditor();
        CollectionProperty property = items.getCollection();
        if (property.getValues().size() == 0) {
            // no invoice items, so add one
            IMObject item = items.create();
            if (item != null) {
                IMObjectEditor editor = items.createEditor(item, getLayoutContext());
                items.addEdited(editor);
                items.editSelected();
            }
        }
    }

    /**
     * Calculates the fixed and unit costs.
     */
    private void calculateCosts() {
        Property fixedCost = getProperty("fixedCost");
        BigDecimal fixed = ActHelper.sum((Act) getObject(),
                                         getEditor().getCurrentActs(),
                                         "fixedCost");
        fixedCost.setValue(fixed);

        Property unitCost = getProperty("unitCost");
        BigDecimal cost = BigDecimal.ZERO;
        for (Act act : getEditor().getCurrentActs()) {
            cost = cost.add(calcTotalUnitCost(act));
        }
        unitCost.setValue(cost);
    }

    /**
     * Calculates the total unit cost for an act, based on its <em>unitCost</em>
     * and <em>quantity</em>.
     *
     * @param act the act
     * @return the total unit cost
     */
    private BigDecimal calcTotalUnitCost(Act act) {
        IMObjectBean bean = new IMObjectBean(act);
        BigDecimal unitCost = bean.getBigDecimal("unitCost", BigDecimal.ZERO);
        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        return unitCost.multiply(quantity);
    }

}