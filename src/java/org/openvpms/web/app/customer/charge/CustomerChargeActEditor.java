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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
     * Determines if a default item should added if no items are present.
     */
    private final boolean addDefaultItem;

    /**
     * Constructs a <tt>CustomerChargeActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public CustomerChargeActEditor(FinancialAct act, IMObject parent,
                                   LayoutContext context) {
        this(act, parent, context, true);
    }

    /**
     * Constructs a <tt>CustomerChargeActEditor</tt>.
     *
     * @param act            the act to edit
     * @param parent         the parent object. May be <tt>null</tt>
     * @param context        the layout context
     * @param addDefaultItem if <tt>true</tt> add a default item if the act has none
     */
    public CustomerChargeActEditor(FinancialAct act, IMObject parent,
                                   LayoutContext context, boolean addDefaultItem) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
        this.addDefaultItem = addDefaultItem;
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
     * Returns any unprinted documents that are flagged for immediate printing.
     *
     * @return the list of unprinted documents
     */
    public List<Act> getUnprintedDocuments() {
        return getUnprintedDocuments(Collections.<Act>emptyList());
    }

    /**
     * Returns any unprinted documents that are flagged for immediate printing.
     *
     * @param exclude a list of documents to ignore
     * @return the list of unprinted documents
     */
    public List<Act> getUnprintedDocuments(List<Act> exclude) {
        List<Act> result = new ArrayList<Act>();
        ActRelationshipCollectionEditor items = getEditor();
        Set<IMObjectReference> excludeRefs = new HashSet<IMObjectReference>();
        for (Act excluded : exclude) {
            excludeRefs.add(excluded.getObjectReference());
        }
        for (Act item : items.getActs()) {
            ActBean bean = new ActBean(item);
            for (ActRelationship rel : bean.getValues("documents", ActRelationship.class)) {
                IMObjectReference target = rel.getTarget();
                if (target != null && !excludeRefs.contains(target)) {
                    Act document = (Act) getObject(target);
                    if (document != null) {
                        ActBean documentBean = new ActBean(document);
                        if (!documentBean.getBoolean("printed") && documentBean.hasNode("documentTemplate")) {
                            Entity entity = (Entity) getObject(documentBean.getNodeParticipantRef("documentTemplate"));
                            if (entity != null) {
                                DocumentTemplate template = new DocumentTemplate(entity,
                                                                                 ServiceHelper.getArchetypeService());
                                if (template.getPrintMode() == DocumentTemplate.PrintMode.IMMEDIATE) {
                                    result.add(document);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds a new charge item, returning its editor.
     *
     * @return the charge item editor, or <tt>null</tt> if an item couldn't be created
     */
    public CustomerChargeActItemEditor addItem() {
        CustomerChargeActItemEditor result = null;
        ActRelationshipCollectionEditor items = getEditor();
        IMObject item = items.create();
        if (item != null) {
            result = (CustomerChargeActItemEditor) items.getEditor(item);
            result.getComponent();
            items.addEdited(result);
            items.editSelected();
            // set the default focus to that of the item editor
            getFocusGroup().setDefault(result.getFocusGroup().getDefaultFocus());
        }
        return result;
    }

    /**
     * Save any edits.
     * <p/>
     * This links items to their corresponding clinical events, creating events as required, and marks matching
     * reminders completed.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        List<Act> reminders = getNewReminders();
        boolean saved = super.doSave();
        if (saved) {
            // link the items to their corresponding clinical events
            ChargeItemEventLinker linker = new ChargeItemEventLinker(ServiceHelper.getArchetypeService());
            List<FinancialAct> items = new ArrayList<FinancialAct>();
            for (Act act : getEditor().getActs()) {
                items.add((FinancialAct) act);
            }
            linker.link(items);

            // mark reminders that match the new reminders completed
            if (!reminders.isEmpty()) {
                ReminderRules rules = new ReminderRules(ServiceHelper.getArchetypeService());
                rules.markMatchingRemindersCompleted(reminders);
            }
        }
        return super.doSave();
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
     * Adds a default invoice item if there are no items present and {@link #addDefaultItem} is <tt>true</tt>.
     */
    private void initItems() {
        if (addDefaultItem) {
            ActRelationshipCollectionEditor items = getEditor();
            CollectionProperty property = items.getCollection();
            if (property.getValues().size() == 0) {
                // no invoice items, so add one
                addItem();
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

    /**
     * Returns new reminders from each of the charge items.
     *
     * @return a list of new reminders
     */
    private List<Act> getNewReminders() {
        ActRelationshipCollectionEditor items = getEditor();
        List<Act> reminders = new ArrayList<Act>();
        for (IMObjectEditor editor : items.getCurrentEditors()) {
            if (editor instanceof CustomerChargeActItemEditor) {
                CustomerChargeActItemEditor charge = (CustomerChargeActItemEditor) editor;
                for (Act reminder : charge.getReminders()) {
                    if (reminder.isNew()) {
                        reminders.add(reminder);
                    }
                }
            }
        }
        return reminders;
    }

}