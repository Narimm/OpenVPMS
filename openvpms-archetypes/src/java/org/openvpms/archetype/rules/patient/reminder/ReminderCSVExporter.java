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

package org.openvpms.archetype.rules.patient.reminder;

import au.com.bytecode.opencsv.CSVWriter;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

/**
 * Exports reminders to CSV.
 *
 * @author Tim Anderson
 */
public class ReminderCSVExporter implements ReminderExporter {

    /**
     * The CSV header line.
     */
    public static final String[] HEADER = {
            "Customer Identifier", "Customer Title", "Customer First Name", "Customer Initials", "Customer Surname",
            "Company Name", "Customer Street Address", "Customer Suburb", "Customer State", "Customer Postcode",
            "Customer Phone", "Customer Email",
            "Patient Identifier", "Patient Name", "Patient Species", "Patient Breed", "Patient Sex", "Patient Colour",
            "Patient Date of Birth", "Reminder Type Identifier", "Reminder Type Name", "Reminder Due Date",
            "Reminder Count", "Reminder Last Sent Date"};

    /**
     * The practice rules.
     */
    private final PracticeRules practiceRules;

    /**
     * The party rules.
     */
    private final PartyRules partyRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * The mime type of the exported document.
     */
    private static final String MIME_TYPE = "text/csv";


    /**
     * Constructs a {@link ReminderCSVExporter}.
     *
     * @param practiceRules the practice rules
     * @param partyRules    the party rules
     * @param service       the archetype service
     * @param lookups       the lookup service
     * @param handlers      the document handlers
     */
    public ReminderCSVExporter(PracticeRules practiceRules, PartyRules partyRules, IArchetypeService service,
                               ILookupService lookups, DocumentHandlers handlers) {
        this.practiceRules = practiceRules;
        this.partyRules = partyRules;
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
    }

    /**
     * Exports reminders to CSV.
     *
     * @param reminders the reminders to export
     * @return the exported reminders
     */
    public Document export(List<ReminderEvent> reminders) {
        char separator = getSeparator();
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, separator);
        csv.writeNext(HEADER);
        for (ReminderEvent event : reminders) {
            if (event.getReminderType() != null && event.getCustomer() != null
                && TypeHelper.isA(event.getContact(), ContactArchetypes.LOCATION) && event.getPatient() != null) {
                export(event, csv);
            }
        }
        String name = "reminders-" + new java.sql.Date(System.currentTimeMillis()).toString() + ".csv";

        DocumentHandler handler = handlers.get(name, MIME_TYPE);
        byte[] buffer = writer.getBuffer().toString().getBytes(Charset.forName("UTF-8"));
        return handler.create(name, new ByteArrayInputStream(buffer), MIME_TYPE, buffer.length);
    }

    /**
     * Returns the field separator.
     *
     * @return the field separator
     */
    public char getSeparator() {
        char separator = ',';
        Party practice = practiceRules.getPractice();
        if (practice != null) {
            IMObjectBean bean = new IMObjectBean(practice, service);
            if ("TAB".equals(bean.getString("reminderExportFormat"))) {
                separator = '\t';
            }
        }
        return separator;
    }

    /**
     * Exports a reminder.
     *
     * @param event  the reminder event to export
     * @param writer the writer to export to
     */
    private void export(ReminderEvent event, CSVWriter writer) {
        IMObjectBean customer = new IMObjectBean(event.getCustomer(), service);
        IMObjectBean location = new IMObjectBean(event.getContact(), service);
        IMObjectBean patient = new IMObjectBean(event.getPatient(), service);
        IMObjectBean reminder = new IMObjectBean(event.getReminder(), service);
        ReminderType reminderType = event.getReminderType();

        String customerId = Long.toString(customer.getObject().getId());
        String title = getLookup(customer, "title");
        String firstName = customer.getString("firstName");
        String initials = customer.getString("initials");
        String lastName = customer.getString("lastName");
        String companyName = customer.getString("companyName");
        String address = location.getString("address");
        String suburb = getLookup(location, "suburb");
        String state = getLookup(location, "state");
        String postCode = location.getString("postcode");
        String phone = partyRules.getTelephone(event.getCustomer());
        String email = partyRules.getEmailAddress(event.getCustomer());
        String patientId = Long.toString(event.getPatient().getId());
        String patientName = patient.getString("name");
        String species = getLookup(patient, "species");
        String breed = getLookup(patient, "breed");
        String sex = getLookup(patient, "sex");
        String colour = patient.getString("colour");
        String dateOfBirth = getDate(patient.getDate("dateOfBirth"));
        String reminderTypeId = Long.toString(reminderType.getEntity().getId());
        String reminderTypeName = reminderType.getName();
        String dueDate = getDate(event.getReminder().getActivityEndTime());
        String reminderCount = reminder.getString("reminderCount");
        String lastSentDate = getDate(reminder.getDate("lastSent"));

        String[] line = {customerId, title, firstName, initials, lastName, companyName, address, suburb, state,
                         postCode, phone, email, patientId, patientName, species, breed, sex, colour, dateOfBirth,
                         reminderTypeId, reminderTypeName, dueDate, reminderCount, lastSentDate};
        writer.writeNext(line);
    }

    /**
     * Helper to return a date as a string.
     *
     * @param date the date. May be {@code null}
     * @return the date as a string. May be {@code null}
     */
    private String getDate(Date date) {
        return (date != null) ? new java.sql.Date(date.getTime()).toString() : null;
    }

    /**
     * Returns the name for a lookup node.
     *
     * @param bean the bean
     * @param node the node
     * @return the lookup name
     */
    private String getLookup(IMObjectBean bean, String node) {
        return LookupHelper.getName(service, lookups, bean.getObject(), node);
    }

}