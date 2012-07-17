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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.charge;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Visit charge editor.
 * <p/>
 * This displays the total amount and tax amount for the current patient.
 *
 * @author Tim Anderson
 */
public class VisitChargeEditor extends AbstractCustomerChargeActEditor {

    /**
     * The event to link charge items to.
     */
    private Act event;

    /**
     * The total charge for the patient for the visit.
     */
    private final SimpleProperty visitTotal;

    /**
     * The total tax for the patient for the visit.
     */
    private final SimpleProperty visitTax;

    /**
     * Filters the amount, tax, reference and printed nodes.
     */
    private static final NodeFilter filter = new NamedNodeFilter("amount", "tax", "reference", "printed");


    /**
     * Constructs a {@code VisitChargeActEditor}.
     *
     * @param act     the act to edit
     * @param event   the event to link charge items to
     * @param context the layout context
     */
    public VisitChargeEditor(FinancialAct act, Act event, LayoutContext context) {
        this(act, event, context, true);
    }

    /**
     * Constructs a {@code VistitChargeEditor}.
     *
     * @param act            the act to edit
     * @param event          the event to link charge items to
     * @param context        the layout context
     * @param addDefaultItem if{@code true} add a default item if the act has none
     */
    public VisitChargeEditor(FinancialAct act, Act event, LayoutContext context, boolean addDefaultItem) {
        super(act, null, context, addDefaultItem);
        this.event = event;
        visitTotal = new SimpleProperty("visitTotal", BigDecimal.ZERO, Money.class);
        visitTotal.setReadOnly(true);
        visitTax = new SimpleProperty("visitTax", BigDecimal.ZERO, Money.class);
        visitTax.setReadOnly(true);
        calculateVisitTotals();
    }

    /**
     * Updates the amount and tax when an act item changes.
     */
    @Override
    protected void onItemsChanged() {
        super.onItemsChanged();
        calculateVisitTotals();
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
        return new VisitChargeItemRelationshipCollectionEditor(items, act, getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ActLayoutStrategy(getItems()) {
            @Override
            protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
                return FilterHelper.chain(filter, context.getDefaultNodeFilter());
            }

            /**
             * Creates a component for a property.
             * <p/>
             * This makes the status node read-only.
             *
             * @param property the property
             * @param parent   the parent object
             * @param context  the layout context
             * @return a component to display <tt>property</tt>
             */
            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                if ("status".equals(property.getName())) {
                    // status is not editable
                    return super.createComponent(createReadOnly(property), parent, context);
                }
                return super.createComponent(property, parent, context);
            }

            @Override
            protected ComponentSet createComponentSet(IMObject object, List<NodeDescriptor> descriptors,
                                                      PropertySet properties, LayoutContext context) {
                ComponentSet result = super.createComponentSet(object, descriptors, properties, context);
                IMObjectComponentFactory factory = context.getComponentFactory();

                ComponentState total = factory.create(visitTotal, object);
                ComponentState tax = factory.create(visitTax, object);

                total.setDisplayName(Messages.get("patient.record.charge.total"));
                tax.setDisplayName(Messages.get("patient.record.charge.tax"));

                result.add(1, total);
                result.add(2, tax);

                return result;
            }
        };
    }

    /**
     * Save any edits.
     * <p/>
     * For invoices, this links items to their corresponding clinical events, creating events as required, and marks
     * matching reminders completed.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean result = super.doSave();
        if (result) {
            addTemplateNotes();
        }
        return result;
    }

    /**
     * Links the charge items to their corresponding clinical events.
     */
    @Override
    protected void linkToEvents() {
        List<FinancialAct> items = getItems().getPatientActs();
        event = IMObjectHelper.reload(event); // make sure the most recent instance is being used
        if (event != null && !items.isEmpty()) {
            // make sure the current event is being used
            ChargeItemEventLinker linker = new ChargeItemEventLinker(null, null,
                                                                     ServiceHelper.getArchetypeService());
            linker.link(event, items);
        }
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    protected VisitChargeItemRelationshipCollectionEditor getItems() {
        return (VisitChargeItemRelationshipCollectionEditor) super.getItems();
    }

    /**
     * Calculates the total amount and tax for the patient.
     */
    private void calculateVisitTotals() {
        VisitChargeItemRelationshipCollectionEditor items = getItems();
        List<FinancialAct> acts = items.getCurrentPatientActs();
        BigDecimal total = ActHelper.sum((Act) getObject(), acts, "total");
        visitTotal.setValue(total);

        BigDecimal tax = ActHelper.sum((Act) getObject(), acts, "tax");
        visitTax.setValue(tax);
    }

    /**
     * Creates <em>act.patientClinicalNote</em> acts for any notes associated with template products, linking them to
     * the event.
     */
    private void addTemplateNotes() {
        List<TemplateChargeItems> templates = getItems().getTemplates();
        if (event != null && !templates.isEmpty()) {
            List<FinancialAct> items = getItems().getPatientActs();
            MedicalRecordRules rules = new MedicalRecordRules(ServiceHelper.getArchetypeService());
            for (TemplateChargeItems template : templates) {
                Act item = template.findFirst(items);
                if (item != null) {
                    String visitNote = template.getVisitNote();
                    if (!StringUtils.isEmpty(visitNote)) {
                        ActBean bean = new ActBean(item);
                        Date itemStartTime = bean.getDate("startTime");
                        Date startTime = getStartTime();
                        if (DateRules.getDate(itemStartTime).compareTo(DateRules.getDate(startTime)) != 0) {
                            // use the item start time if its date is different to that of the invoice
                            startTime = itemStartTime;
                        }
                        User clinician = (User) getObject(bean.getNodeParticipantRef("clinician"));
                        User author = (User) getObject(bean.getNodeParticipantRef("author"));
                        rules.addNote(event, startTime, visitNote, clinician, author);
                    }
                }
            }
            getItems().clearTemplates();
        }
    }
}
