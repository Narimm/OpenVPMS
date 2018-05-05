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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.reminder;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.jxpath.AbstractObjectFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JXPath extension functions for reminders.
 * <p/>
 * This extends {@link AbstractObjectFunctions} in order to translate the "getReminder" function to the appropriate
 * implementation to avoid ambiguous method calls.
 *
 * @author Tim Anderson
 */
public class ReminderFunctions extends AbstractObjectFunctions {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ReminderFunctions}.
     *
     * @param archetypeService the archetype service
     * @param rules            the reminder rules
     * @param customerRules    the customer rules
     */
    public ReminderFunctions(IArchetypeService archetypeService, ReminderRules rules, CustomerRules customerRules) {
        super("reminder");
        setObject(this);
        this.service = archetypeService;
        this.rules = rules;
        this.customerRules = customerRules;
    }

    /**
     * Returns reminders for a customer's patients for the customer associated with the supplied act.
     *
     * @param act         the act
     * @param dueInterval the due interval, relative to the current date
     * @param dueUnits    the due interval units
     * @return the reminders for the customer's patients
     */
    public Iterable<Act> getReminders(Act act, int dueInterval, String dueUnits) {
        return getReminders(act, dueInterval, dueUnits, false);
    }

    /**
     * Returns reminders for a customer's patients for the customer associated with the supplied act.
     *
     * @param act            the act
     * @param dueInterval    the due interval, relative to the current date
     * @param dueUnits       the due interval units
     * @param includeOverdue if {@code true}, include reminders that are overdue
     * @return the reminders for the customer's patients
     */
    public Iterable<Act> getReminders(Act act, int dueInterval, String dueUnits, boolean includeOverdue) {
        Iterable<Act> result;
        ActBean bean = new ActBean(act, service);
        Party customer = (Party) bean.getParticipant(CustomerArchetypes.CUSTOMER_PARTICIPATION);
        if (customer != null) {
            result = getReminders(customer, dueInterval, dueUnits, includeOverdue);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns reminders for the specified customer's patients.
     *
     * @param customer    the customer
     * @param dueInterval the due interval, relative to the current date
     * @param dueUnits    the due interval units
     * @return the reminders for the customer's patients
     */
    public Iterable<Act> getReminders(Party customer, int dueInterval, String dueUnits) {
        return getReminders(customer, dueInterval, dueUnits, false);
    }

    /**
     * Returns reminders for the specified customer's patients.
     *
     * @param customer       the customer
     * @param dueInterval    the due interval, relative to the current date
     * @param dueUnits       the due interval units
     * @param includeOverdue if {@code true}, include reminders that are overdue
     * @return the reminders for the customer's patients
     */
    public Iterable<Act> getReminders(Party customer, int dueInterval, String dueUnits, boolean includeOverdue) {
        List<Act> result;
        if (customer != null) {
            result = new ArrayList<>();
            DateUnits units = DateUnits.valueOf(dueUnits);
            List<Act> reminders = customerRules.getReminders(customer, dueInterval, units, includeOverdue);
            for (Act reminder : reminders) {
                ActBean bean = new ActBean(reminder, service);
                Party patient = (Party) bean.getNodeParticipant("patient");

                // exclude reminders with inactive or deceased patients
                if (patient != null && patient.isActive()) {
                    IMObjectBean patientBean = new IMObjectBean(patient, service);
                    if (!patientBean.getBoolean("deceased")) {
                        result.add(reminder);
                    }
                }
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns a reminder associated with an <em>act.patientDocumentForm</em>.
     *
     * @param form the form
     * @return the reminder, or {@code null} if there are no associated reminders
     */
    public Act getDocumentFormReminder(DocumentAct form) {
        return rules.getDocumentFormReminder(form);
    }

    /**
     * Returns all reminders for a patient starting on the specified date.
     *
     * @param patient the patient
     * @param date    the date
     * @return all reminders for the patient starting on the specified date
     */
    public Iterable<Act> getPatientReminders(Party patient, Date date) {
        if (patient != null && date != null) {
            Date from = DateRules.getDate(date);
            Date to = DateRules.getDate(from, 1, DateUnits.DAYS);
            return getPatientReminders(patient, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns all reminders for a patient starting in the specified date range.
     *
     * @param patient the patient
     * @param from    the start of the date range, inclusive
     * @param to      the end of the date range, exclusive
     * @return all reminders for the patient in the date range
     */
    public Iterable<Act> getPatientReminders(Party patient, Date from, Date to) {
        if (patient != null && from != null && to != null) {
            return rules.getReminders(patient, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns all reminders for a patient and product type starting on the specified date.
     *
     * @param patient     the patient
     * @param productType the product type. May contain wildcards.
     * @param date        the date
     * @return all reminders for the patient starting on the specified date
     */
    public Iterable<Act> getRemindersByProductType(Party patient, String productType, Date date) {
        if (patient != null && date != null && productType != null) {
            Date from = DateRules.getDate(date);
            Date to = DateRules.getDate(from, 1, DateUnits.DAYS);
            return getRemindersByProductType(patient, productType, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns all reminders for a patient starting in the specified date range.
     *
     * @param patient     the patient
     * @param productType the product type. May contain wildcards.
     * @param from        the start of the date range, inclusive
     * @param to          the end of the date range, exclusive
     * @return all reminders for the patient in the date range
     */
    public Iterable<Act> getRemindersByProductType(Party patient, String productType, Date from, Date to) {
        if (patient != null && from != null && to != null && productType != null) {
            return rules.getReminders(patient, productType, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns a Function, if any, for the specified namespace name and parameter types.
     * <p/>
     * This implementation changes getReminder to:
     * <ul>
     * <li>getRemindersByProductType if the first argument is a patient and the last is a string; or</li>
     * <li>getPatientReminders if the first argument is a patient</li>
     * </ul>
     * This is required as JXPath can't cannot resolve which method to call if they are all named getReminders().
     *
     * @param namespace if it is not the namespace specified in the constructor, the method returns null
     * @param name      is a function name.
     * @return a MethodFunction, or {@code null} if there is no such function.
     */
    @Override
    public Function getFunction(String namespace, String name, Object[] parameters) {
        if ("getReminders".equals(name) && hasPatient(parameters)) {
            if ((parameters.length == 3 || parameters.length == 4) && parameters[1] instanceof String) {
                name = "getRemindersByProductType";
            } else {
                name = "getPatientReminders";
            }
        }
        return super.getFunction(namespace, name, parameters);
    }

    /**
     * Helper to determine if the first in a list of parameters is a patient
     *
     * @param parameters the parameters
     * @return {@code true} if the first parameter is a patient
     */
    private boolean hasPatient(Object[] parameters) {
        boolean result = false;
        if (parameters != null && parameters.length > 0) {
            Object parameter = parameters[0];
            if (isPatient(parameter)) {
                result = true;
            } else if (parameter instanceof NodeSet) {
                List pointers = ((NodeSet) parameter).getPointers();
                if (pointers != null && pointers.size() == 1 && isPatient(pointers.get(0))) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Helper to determine if a parameter is a patient.
     *
     * @param parameter the parameter
     * @return {@code true} if the parameter is a patient
     */
    private boolean isPatient(Object parameter) {
        if (parameter instanceof Pointer) {
            parameter = ((Pointer) parameter).getValue();
        }
        return parameter instanceof IMObject && TypeHelper.isA((IMObject) parameter, PatientArchetypes.PATIENT);
    }

}
