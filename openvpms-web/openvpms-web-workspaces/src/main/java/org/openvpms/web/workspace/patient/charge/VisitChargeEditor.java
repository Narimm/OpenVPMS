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

package org.openvpms.web.workspace.patient.charge;

import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Visit charge editor.
 * <p/>
 * This displays the total amount and tax amount for the current patient.
 *
 * @author Tim Anderson
 */
public class VisitChargeEditor extends CustomerChargeActEditor {

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
     * Filters the amount, tax, and printed nodes.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("amount", "tax", "printed");

    /**
     * Constructs a {@link VisitChargeEditor}.
     *
     * @param act     the act to edit
     * @param event   the event to link charge items to
     * @param context the layout context
     */
    public VisitChargeEditor(FinancialAct act, Act event, LayoutContext context) {
        this(act, event, context, true);
    }

    /**
     * Constructs a {@link VisitChargeEditor}.
     *
     * @param act            the act to edit
     * @param event          the event to link charge items to
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
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
     * Returns the event to link charge items to.
     *
     * @return the event to link charge items to
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        return new VisitChargeEditor(reload(getObject()), reload(event), getLayoutContext(), getAddDefaultIem());
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
     * Creates a collection editor for the customer notes collection.
     *
     * @param act   the act
     * @param notes the customer notes collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createCustomerNotesEditor(Act act, CollectionProperty notes) {
        return new VisitActRelationshipCollectionEditor(notes, act, getLayoutContext());
    }

    /**
     * Creates a collection editor for the documents collection.
     *
     * @param act       the act
     * @param documents the documents collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createDocumentsEditor(Act act, CollectionProperty documents) {
        return new VisitActRelationshipCollectionEditor(documents, act, getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        VisitChargeLayoutStrategy strategy = new VisitChargeLayoutStrategy();
        initLayoutStrategy(strategy);
        return strategy;
    }

    /**
     * Links the charge items to their corresponding clinical events.
     *
     * @param changes the patient history changes
     */
    @Override
    protected void linkToEvents(PatientHistoryChanges changes) {
        List<Act> items = getItems().getPatientActs();
        if (!items.isEmpty()) {
            event = reload(event); // make sure the most recent instance is being used
            changes.addEvent(event);
            ChargeItemEventLinker linker = new ChargeItemEventLinker(ServiceHelper.getArchetypeService());
            linker.prepare(event, items, changes);
            addTemplateNotes(linker, changes);
        }
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    public VisitChargeItemRelationshipCollectionEditor getItems() {
        return (VisitChargeItemRelationshipCollectionEditor) super.getItems();
    }

    /**
     * Calculates the total amount and tax for the patient.
     */
    private void calculateVisitTotals() {
        VisitChargeItemRelationshipCollectionEditor items = getItems();
        List<Act> acts = items.getCurrentPatientActs();
        BigDecimal total = ActHelper.sum(getObject(), acts, "total");
        visitTotal.setValue(total);

        BigDecimal tax = ActHelper.sum(getObject(), acts, "tax");
        visitTax.setValue(tax);
    }

    private class VisitChargeLayoutStrategy extends ActLayoutStrategy {

        public VisitChargeLayoutStrategy() {
            super(VisitChargeEditor.this.getItems(), NODES);
        }

        /**
         * Creates a component for a property.
         * <p/>
         * This makes the status node read-only.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display {@code property}
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
        protected ComponentSet createComponentSet(IMObject object, List<Property> properties,
                                                  LayoutContext context) {
            ComponentSet result = super.createComponentSet(object, properties, context);
            IMObjectComponentFactory factory = context.getComponentFactory();

            ComponentState total = factory.create(visitTotal, object);
            ComponentState tax = factory.create(visitTax, object);

            total.setDisplayName(Messages.get("patient.record.charge.total"));
            tax.setDisplayName(Messages.get("patient.record.charge.tax"));

            result.add(1, total);
            result.add(2, tax);

            return result;
        }

    }
}
