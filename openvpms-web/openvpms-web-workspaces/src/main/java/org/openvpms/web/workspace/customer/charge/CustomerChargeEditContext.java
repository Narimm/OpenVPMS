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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.StockOnHand;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Edit context for customer charges.
 *
 * @author Tim Anderson
 */
public class CustomerChargeEditContext extends ChargeEditContext {

    /**
     * The save context.
     */
    private final ChargeSaveContext saveContext;

    /**
     * The alerts.
     */
    private final Alerts alerts;

    /**
     * The stock on hand.
     */
    private final StockOnHand stock;

    /**
     * The reminder rules.
     */
    private final ReminderRules reminderRules;

    /**
     * The prescriptions. May be {@code null}.
     */
    private Prescriptions prescriptions;

    /**
     * Constructs a {@link CustomerChargeEditContext}.
     *
     * @param customer the customer
     * @param location the practice location. May be {@code null}
     * @param context  the layout context
     */
    public CustomerChargeEditContext(Party customer, Party location, LayoutContext context) {
        super(customer, location, context);
        saveContext = new ChargeSaveContext();
        reminderRules = new ReminderRules(getCachingArchetypeService(), ServiceHelper.getBean(PatientRules.class));
        stock = new StockOnHand(getStockRules());
        alerts = new Alerts();
    }

    /**
     * Returns the save context.
     *
     * @return the save context
     */
    public ChargeSaveContext getSaveContext() {
        return saveContext;
    }

    /**
     * Returns the stock on hand.
     *
     * @return the stock on hand
     */
    public StockOnHand getStock() {
        return stock;
    }

    /**
     * Sets the prescriptions.
     *
     * @param prescriptions the prescriptions. May be {@code null}
     */
    public void setPrescriptions(Prescriptions prescriptions) {
        this.prescriptions = prescriptions;
    }

    /**
     * Returns the prescriptions.
     *
     * @return the prescriptions. May be {@code null}
     */
    public Prescriptions getPrescriptions() {
        return prescriptions;
    }

    /**
     * Returns the alerts.
     *
     * @return the alerts
     */
    public Alerts getAlerts() {
        return alerts;
    }

    /**
     * Helper to return the reminder types and their relationships for a product.
     * <p>
     * This excludes any reminder type not for the patient species.
     * <p/>
     * If there are multiple reminder types, these will be sorted on name.
     *
     * @param product the product
     * @param patient the patient, used to filter reminder types for a different species. May be {@code}
     * @return a the reminder type relationships
     */
    public Map<Entity, EntityRelationship> getReminderTypes(Product product, Party patient) {
        Map<EntityRelationship, Entity> map = reminderRules.getReminderTypes(product);
        Map<Entity, EntityRelationship> result = new TreeMap<>(IMObjectSorter.getNameComparator(true));
        IArchetypeService service = getCachingArchetypeService();
        String species = (patient != null) ? service.getBean(patient).getString("species") : null;
        for (Map.Entry<EntityRelationship, Entity> entry : map.entrySet()) {
            Entity reminderType = entry.getValue();
            if (species == null || reminderTypeIsForSpecies(reminderType, species)) {
                result.put(reminderType, entry.getKey());
            }
        }
        return result;
    }

    /**
     * Calculates the due date for a product reminder.
     *
     * @param startTime    the start time
     * @param relationship the product reminder relationship
     * @return the due date for the reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getReminderDueDate(Date startTime, EntityRelationship relationship) {
        return reminderRules.calculateProductReminderDueDate(startTime, relationship);
    }

    /**
     * Determines if a reminder type supports the supplied species.
     *
     * @param reminderType the reminder type
     * @param species      the species
     * @return {@code true} if the reminder type supports the supplied species
     */
    private boolean reminderTypeIsForSpecies(Entity reminderType, String species) {
        boolean result = false;
        IMObjectBean bean = getCachingArchetypeService().getBean(reminderType);
        List<Lookup> supported = bean.getValues("species", Lookup.class);
        if (supported.isEmpty()) {
            result = true;
        } else {
            for (Lookup lookup : supported) {
                if (species.equals(lookup.getCode())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
