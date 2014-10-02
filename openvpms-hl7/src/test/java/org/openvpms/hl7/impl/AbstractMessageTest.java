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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.junit.Before;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.PatientContext;
import org.openvpms.hl7.PatientContextFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.openvpms.archetype.test.TestHelper.getLookup;

/**
 * Base class for HL7 message tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMessageTest extends ArchetypeServiceTest {

    /**
     * The patient context.
     */
    private PatientContext context;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        ILookupService lookups = LookupServiceHelper.getLookupService();
        PatientRules rules = new PatientRules(getArchetypeService(), lookups);
        CustomerRules customerRules = new CustomerRules(getArchetypeService());
        Party owner = TestHelper.createCustomer("Foo", "Bar", true);
        Party patient = TestHelper.createPatient(owner);
        Lookup species = getLookup("lookup.species", "CANINE", "Canine", true);
        getLookup("lookup.breed", "KELPIE", "Kelpie", species, "lookupRelationship.speciesBreed");

        IMObjectBean bean = new IMObjectBean(patient);
        owner.getContacts().clear();
        Contact home = TestHelper.createLocationContact("123 Broadwater Avenue", "CAPE_WOOLAMAI", "Cape Woolamai",
                                                        "VIC", "VIC", "3058");
        Lookup homePurpose = TestHelper.getLookup("lookup.contactPurpose", "HOME");
        Lookup workPurpose = TestHelper.getLookup("lookup.contactPurpose", "WORK");
        home.addClassification(homePurpose);
        Contact homePhone = TestHelper.createPhoneContact("03", "12345678");
        homePhone.addClassification(homePurpose);

        Contact workPhone = TestHelper.createPhoneContact("03", "98765432");
        workPhone.addClassification(workPurpose);

        owner.addContact(home);
        owner.addContact(homePhone);
        owner.addContact(workPhone);
        bean.setValue("dateOfBirth", TestHelper.getDate("2014-07-01"));
        Party location = TestHelper.createLocation();
        location.setName("Main Clinic");
        patient.setName("Fido");
        bean.setValue("species", "CANINE");
        bean.setValue("breed", "KELPIE");
        bean.setValue("sex", "MALE");

        Act visit = PatientTestHelper.createEvent(patient, null);
        visit.setActivityStartTime(getDatetime("2014-08-25 08:55:00").getTime());
        save(visit);

        Act weight = PatientTestHelper.createWeight(patient, getDatetime("2014-08-25 08:57:00").getTime(),
                                                    BigDecimal.TEN, WeightUnits.KILOGRAMS);

        save(weight);

        createAllergy(patient, "Penicillin", "Respiratory distress");
        createAllergy(patient, "Pollen", "Produces hives");

        User clinician = TestHelper.createClinician(false);
        clinician.setName("Joe Blogs"); // todo - need separate first, last name fields
        save(clinician);
        PatientContextFactory factory = new PatientContextFactory(rules, customerRules, getArchetypeService(), lookups);
        context = factory.createContext(patient, owner, visit, location, clinician);
        context = Mockito.spy(context);
        Mockito.when(context.getVisitId()).thenReturn(3001L);
        HapiContext hapiContext = new DefaultHapiContext();
        hapiContext.getParserConfiguration().setIdGenerator(new IDGenerator() {
            @Override
            public String getID() throws IOException {
                return "1200022";
            }
        });
    }

    /**
     * Returns the patient context.
     *
     * @return the patient context
     */
    protected PatientContext getContext() {
        return context;
    }

    /**
     * Helper to create a {@code Calendar} from a date-time string in {@code java.sql.Date} format.
     *
     * @param datetime the date-time
     * @return a new {@code Calendar}
     */
    protected Calendar getDatetime(String datetime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(TestHelper.getDatetime(datetime));
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));
        return calendar;
    }

    /**
     * Logs a message.
     *
     * @param text    text
     * @param message the message
     */
    protected void log(String text, Message message) {
        String log;
        try {
            log = message.encode();
            log = log.replaceAll("\\r", "\n");
        } catch (HL7Exception exception) {
            log = exception.getMessage();
        }

        System.out.println(text + " " + log);
    }

    /**
     * Helper to create an allergy record for a patient.
     *
     * @param patient the patient
     * @param reason  the reason
     * @param notes   the allergy notes
     */
    private void createAllergy(Party patient, String reason, String notes) {
        Act alert = (Act) create(PatientArchetypes.ALERT);
        ActBean bean = new ActBean(alert);
        bean.setValue("alertType", TestHelper.getLookup("lookup.patientAlertType", "ALLERGY").getCode());
        bean.addNodeParticipation("patient", patient);
        bean.setValue("reason", reason);
        bean.setValue("notes", notes);
        bean.save();
    }

}
