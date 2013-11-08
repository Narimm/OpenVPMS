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

package org.openvpms.archetype.test;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Unit test helper.
 *
 * @author Tim Anderson
 */
public class TestHelper extends Assert {

    /**
     * Random no. generator for creating unique names.
     */
    private static final Random random = new Random();

    /**
     * Creates a new object.
     *
     * @param shortName the archetype short name
     * @return a new object
     */
    public static IMObject create(String shortName) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Helper to save an object.
     *
     * @param object the object to save
     * @throws ArchetypeServiceException if the service cannot save the object
     * @throws ValidationException       if the object cannot be validated
     */
    public static void save(IMObject object) {
        ArchetypeServiceHelper.getArchetypeService().save(object);
    }

    /**
     * Helper to save an array of objects.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException if the service cannot save the object
     * @throws ValidationException       if the object cannot be validated
     */
    public static void save(IMObject... objects) {
        save(Arrays.asList(objects));
    }

    /**
     * Helper to save a collection of objects.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException if the service cannot save the object
     * @throws ValidationException       if the object cannot be validated
     */
    public static void save(Collection<? extends IMObject> objects) {
        ArchetypeServiceHelper.getArchetypeService().save(objects);
    }

    /**
     * Creates a new customer.
     *
     * @param firstName the customer's first name
     * @param lastName  the customer's surname
     * @param save      if {@code true} make the customer persistent
     * @return a new customer
     */
    public static Party createCustomer(String firstName, String lastName, boolean save) {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        PartyRules rules = new PartyRules(ArchetypeServiceHelper.getArchetypeService());
        customer.setContacts(rules.getDefaultContacts());
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", firstName);
        bean.setValue("lastName", lastName);
        if (save) {
            bean.save();
        }
        return customer;
    }

    /**
     * Creates and saves a new customer.
     *
     * @return a new customer
     */
    public static Party createCustomer() {
        return createCustomer(true);
    }

    /**
     * Creates a new customer.
     *
     * @param save if {@code true} make the customer persistent
     * @return a new customer
     */
    public static Party createCustomer(boolean save) {

        return createCustomer("J", "Zoo-" + nextId(), save);
    }

    /**
     * Creates a new <em>contact.location</em>
     * <p/>
     * Any required lookups will be created and saved.
     *
     * @param address    the street address
     * @param suburbCode the <em>lookup.suburb</em> code
     * @param suburbName the suburb name. May be {@code null}
     * @param stateCode  the <em>lookup.state</em> code
     * @param stateName  the state name. May be {@code null}
     * @param postCode   the post code
     * @return a new location contact
     */
    public static Contact createLocationContact(String address, String suburbCode, String suburbName,
                                                String stateCode, String stateName, String postCode) {
        Lookup state = getLookup("lookup.state", stateCode, stateName, true);
        Lookup suburb = getLookup("lookup.suburb", suburbCode, suburbName, state, "lookupRelationship.stateSuburb");
        Contact contact = (Contact) create(ContactArchetypes.LOCATION);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("address", address);
        bean.setValue("suburb", suburb.getCode());
        bean.setValue("state", state.getCode());
        bean.setValue("postcode", postCode);
        return contact;
    }

    /**
     * Creates a new <em>contact.phoneNumber</em>
     *
     * @param areaCode the area code
     * @param number   the phone number
     * @return a new phone contact
     */
    public static Contact createPhoneContact(String areaCode, String number) {
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("areaCode", areaCode);
        bean.setValue("telephoneNumber", number);
        bean.setValue("preferred", true);
        return contact;
    }

    /**
     * Creates a new <em>contact.email</em>
     *
     * @param address the phone number
     * @return a new email contact
     */
    public static Contact createEmailContact(String address) {
        Contact contact = (Contact) create(ContactArchetypes.EMAIL);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("emailAddress", address);
        bean.setValue("preferred", true);
        return contact;
    }

    /**
     * Creates and saves a new <em>contact.location</em>
     * <p/>
     * Any required lookups will be created and saved.
     *
     * @param address    the street address
     * @param suburbCode the <em>lookup.suburb</em> code
     * @param stateCode  the <em>lookup.state</em> code
     * @param postCode   the post code
     * @return a new location contact
     */
    public static Contact createLocationContact(String address, String suburbCode, String stateCode, String postCode) {
        return createLocationContact(address, suburbCode, null, stateCode, null, postCode);
    }

    /**
     * Creates and saves a new patient, with species='CANINE'.
     *
     * @return a new patient
     */
    public static Party createPatient() {
        return createPatient(true);
    }

    /**
     * Creates a new patient, with species='CANINE'.
     *
     * @param save if {@code true} make the patient persistent
     * @return a new patient
     */
    public static Party createPatient(boolean save) {
        Party patient = (Party) create(PatientArchetypes.PATIENT);
        EntityBean bean = new EntityBean(patient);
        bean.setValue("name", "XPatient-" + nextId());
        bean.setValue("species", "CANINE");
        bean.setValue("deceased", false);
        if (save) {
            bean.save();
        }
        return patient;
    }

    /**
     * Creates and saves a new patient, owned by the specified customer.
     *
     * @param owner the patient owner
     * @return a new patient
     */
    public static Party createPatient(Party owner) {
        return createPatient(owner, true);
    }

    /**
     * Creates a new patient, owned by the specified customer.
     *
     * @param owner the patient owner
     * @param save  if {@code true}, make the patient persistent
     * @return a new patient
     */
    public static Party createPatient(Party owner, boolean save) {
        Party patient = createPatient(save);
        PatientRules rules = new PatientRules(ArchetypeServiceHelper.getArchetypeService(), null, null);
        rules.addPatientOwnerRelationship(owner, patient);
        if (save) {
            save(patient);
        }
        return patient;
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient for the current date, and saves it.
     *
     * @param patient the patient
     * @param weight  the weight
     * @return the weight act
     */
    public static Act createWeight(Party patient, BigDecimal weight, WeightUnits units) {
        return createWeight(patient, new Date(), weight, units);
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient and saves it.
     *
     * @param patient the patient
     * @param date    the date
     * @param weight  the weight
     * @param units   the weight units
     * @return the weight act
     */
    public static Act createWeight(Party patient, Date date, BigDecimal weight, WeightUnits units) {
        Act act = (Act) create(PatientArchetypes.PATIENT_WEIGHT);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.setValue("startTime", date);
        bean.setValue("weight", weight);
        bean.setValue("units", units.toString());
        save(act);
        return act;
    }


    /**
     * Creates and saves a new user.
     *
     * @return a new user
     */
    public static User createUser() {
        // use an int to avoid exceeding the length of the db field
        return createUser("zuser" + Math.abs((int) System.nanoTime()), true);
    }

    /**
     * Creates a new user.
     *
     * @param username the login name
     * @param save     if {@code true} make the user persistent
     * @return a new user
     */
    public static User createUser(String username, boolean save) {
        User user = (User) create(UserArchetypes.USER);
        EntityBean bean = new EntityBean(user);
        bean.setValue("name", username);
        bean.setValue("username", username);
        bean.setValue("password", username);
        if (save) {
            bean.save();
        }
        return user;
    }

    /**
     * Creates and saves a new clinician.
     *
     * @return a new clinician
     */
    public static User createClinician() {
        return createClinician(true);
    }

    /**
     * Creates a new clinician.
     *
     * @param save if {@code true} make the user persistent
     * @return a new user
     */
    public static User createClinician(boolean save) {
        String username = "zuser" + nextId();
        User user = createUser(username, false);
        user.addClassification(getLookup("lookup.userType", "CLINICIAN"));
        if (save) {
            save(user);
        }
        return user;
    }

    /**
     * Creates a new <em>product.medication</em> with no species classification.
     * The product name is prefixed with <em>XProduct-</em>.
     *
     * @return a new product
     */
    public static Product createProduct() {
        return createProduct(null);
    }

    /**
     * Creates a new <em>product.medicication</em> with an optional species
     * classification. The product name is prefixed with <em>XProduct-</em>.
     *
     * @param species the species classification. May be {@code null}
     * @return a new product
     */
    public static Product createProduct(String species) {
        return createProduct(ProductArchetypes.MEDICATION, species);
    }

    /**
     * Creates and saves a new product with an optional species classification.
     * The product name is prefixed with <em>XProduct-</em>.
     *
     * @param shortName the archetype short name
     * @param species   the species classification name. May be {@code null}
     * @return a new product
     */
    public static Product createProduct(String shortName, String species) {
        return createProduct(shortName, species, true);
    }

    /**
     * Creates a new product with an optional species classification.
     * The product name is prefixed with <em>XProduct-</em>.
     *
     * @param shortName the product short name
     * @param species   the species classification name. May be {@code null}
     * @param save      if {@code true}, save the product
     * @return a new product
     */
    public static Product createProduct(String shortName, String species,
                                        boolean save) {
        Product product = (Product) create(shortName);
        EntityBean bean = new EntityBean(product);
        String name = "XProduct-" + ((species != null) ? species : "") + nextId();
        bean.setValue("name", name);
        if (species != null) {
            Lookup classification
                    = getLookup("lookup.species", species);
            bean.addValue("species", classification);
        }
        if (save) {
            bean.save();
        }
        return product;
    }

    /**
     * Creates and saves new <em>party.supplierorganisation</em>.
     *
     * @return a new party
     */
    public static Party createSupplier() {
        return createSupplier(true);
    }

    /**
     * Creates a new <em>party.supplierorganisation</em>.
     *
     * @param save if {@code true} save the supplier
     * @return a new party
     */
    public static Party createSupplier(boolean save) {
        Party party = (Party) create(SupplierArchetypes.SUPPLIER_ORGANISATION);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("name", "XSupplier");
        if (save) {
            bean.save();
        }
        return party;
    }

    /**
     * Creates a new <em>party.supplierVeterinarian</em>.
     *
     * @return a new party
     */
    public static Party createSupplierVet() {
        Party party = (Party) create(SupplierArchetypes.SUPPLIER_VET);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "XSupplierVet");
        bean.setValue("title", "MR");
        bean.save();
        return party;
    }

    /**
     * Creates a new <em>party.supplierVeterinaryPractice</em>.
     *
     * @return a new party
     */
    public static Party createSupplierVetPractice() {
        Party party = (Party) create(SupplierArchetypes.SUPPLIER_VET_PRACTICE);
        party.setName("XVetPractice");
        save(party);
        return party;
    }

    /**
     * Returns the <em>party.organisationPractice</em> singleton,
     * creating one if it doesn't exist.
     * <p/>
     * If it exists, any tax rates will be removed.
     * <p/>
     * The practice currency is set to <em>AUD</em>.
     * <p/>
     * Default contacts are added.
     *
     * @return the practice
     */
    public static Party getPractice() {
        Party party;
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE, true, true);
        query.setMaxResults(1);
        QueryIterator<Party> iter = new IMObjectQueryIterator<Party>(query);
        if (iter.hasNext()) {
            party = iter.next();

            // remove any taxes
            IMObjectBean bean = new IMObjectBean(party);
            List<Lookup> taxes = bean.getValues("taxes", Lookup.class);
            if (!taxes.isEmpty()) {
                for (Lookup tax : taxes) {
                    bean.removeValue("taxes", tax);
                }
            }
        } else {
            party = (Party) create(PracticeArchetypes.PRACTICE);
            party.setName("XPractice");
        }

        PartyRules rules = new PartyRules(ArchetypeServiceHelper.getArchetypeService());
        party.setContacts(rules.getDefaultContacts());

        Lookup currency = getCurrency("AUD");

        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("currency", currency.getCode());
        bean.save();
        return party;
    }

    /**
     * Returns a currency with the specified currency code, creating it
     * if it doesn't exist.
     *
     * @param code the currency code
     * @return the currency
     */
    public static Lookup getCurrency(String code) {
        Lookup currency = getLookup("lookup.currency", code, false);
        IMObjectBean ccyBean = new IMObjectBean(currency);
        ccyBean.setValue("minDenomination", new BigDecimal("0.05"));
        ccyBean.save();
        return currency;
    }

    /**
     * Creates a new <em>party.organisationLocation}.
     *
     * @return a new location
     */
    public static Party createLocation() {
        return createLocation(false);
    }

    /**
     * Creates a new <em>party.organisationLocation}.
     *
     * @param stockControl if {@code true}, enable stock control for the location
     * @return a new location
     */
    public static Party createLocation(boolean stockControl) {
        Party party = (Party) create(PracticeArchetypes.LOCATION);
        party.setName("XLocation");
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        party.addContact(contact);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("stockControl", stockControl);
        bean.save();
        return party;
    }


    /**
     * Creates a new till.
     *
     * @return the new till
     */
    public static Party createTill() {
        Party till = (Party) TestHelper.create(TillArchetypes.TILL);
        assertNotNull(till);
        till.setName("TillRulesTestCase-Till" + till.hashCode());
        save(till);
        return till;
    }


    /**
     * Returns a lookup, creating and saving it if it doesn't exist.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return the lookup
     */
    public static Lookup getLookup(String shortName, String code) {
        return getLookup(shortName, code, true);
    }

    /**
     * Returns a lookup, creating it if it doesn't exist.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @param save      if {@code true}, save the lookup
     * @return the lookup
     */
    public static Lookup getLookup(String shortName, String code,
                                   boolean save) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);
        query.add(new NodeConstraint("code", code));
        query.setMaxResults(1);
        QueryIterator<Lookup> iter = new IMObjectQueryIterator<Lookup>(query);
        if (iter.hasNext()) {
            return iter.next();
        }
        Lookup lookup = (Lookup) create(shortName);
        lookup.setCode(code);
        if (save) {
            save(lookup);
        }
        return lookup;
    }

    /**
     * Returns a lookup, creating it if it doesn't exist.
     * <p/>
     * If the lookup exists, but the name is different, the name will be updated.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @param name      the lookup name
     * @param save      if {@code true}, save the lookup
     * @return the lookup
     */
    public static Lookup getLookup(String shortName, String code, String name, boolean save) {
        Lookup lookup;
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("code", code));
        query.setMaxResults(1);
        QueryIterator<Lookup> iter = new IMObjectQueryIterator<Lookup>(query);
        if (iter.hasNext()) {
            lookup = iter.next();
            if (!StringUtils.equals(name, lookup.getName())) {
                lookup.setName(name);
            } else {
                save = false;
            }
        } else {
            lookup = (Lookup) create(shortName);
            lookup.setCode(code);
            lookup.setName(name);
        }
        if (save) {
            save(lookup);
        }
        return lookup;
    }

    /**
     * Returns a lookup that is the target in a lookup relationship, creating and saving it if it doesn't exist.
     *
     * @param shortName             the target lookup short name
     * @param code                  the lookup code
     * @param source                the source lookup
     * @param relationshipShortName the lookup relationship short name
     * @return the lookup
     */
    public static Lookup getLookup(String shortName, String code, Lookup source,
                                   String relationshipShortName) {
        return getLookup(shortName, code, code, source, relationshipShortName);
    }

    /**
     * Returns a lookup that is the target in a lookup relationship, creating
     * and saving it if it doesn't exist.
     *
     * @param shortName             the target lookup short name
     * @param code                  the lookup code
     * @param name                  the lookup name
     * @param source                the source lookup
     * @param relationshipShortName the lookup relationship short name
     * @return the lookup
     */
    public static Lookup getLookup(String shortName, String code, String name, Lookup source,
                                   String relationshipShortName) {
        Lookup target = getLookup(shortName, code, name, true);
        for (LookupRelationship relationship : source.getLookupRelationships()) {
            if (relationship.getTarget().equals(target.getObjectReference())) {
                return target;
            }
        }
        LookupRelationship relationship = (LookupRelationship) create(relationshipShortName);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addLookupRelationship(relationship);
        target.addLookupRelationship(relationship);
        save(Arrays.asList(source, target));
        return target;
    }

    /**
     * Helper to return the name of the lookup for the specified object and
     * node.
     *
     * @param object the object
     * @param node   the lookup node
     * @return the corresponding lookup's name. May be {@code null}
     */
    public static String getLookupName(IMObject object, String node) {
        IMObjectBean bean = new IMObjectBean(object);
        return LookupHelper.getName(
                ArchetypeServiceHelper.getArchetypeService(),
                bean.getDescriptor(node),
                object);
    }

    /**
     * Helper to create and save a new tax type classification.
     *
     * @return a new tax classification
     */
    public static Lookup createTaxType(BigDecimal rate) {
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean bean = new IMObjectBean(tax);
        bean.setValue("code", "XTAXTYPE" + System.nanoTime());
        bean.setValue("rate", rate);
        save(tax);
        return tax;
    }

    /**
     * Helper to create a date-time given a string of the form
     * <em>yyyy-mm-dd hh:mm:ss</em>.
     *
     * @param value the value. May be {@code null}
     * @return the corresponding date-time or {@code null} if {@code value} is null
     */
    public static Date getDatetime(String value) {
        return value != null ? new Date(Timestamp.valueOf(value).getTime()) : null; // use Date, for easy comparison
    }

    /**
     * Helper to create a date given a string of the form <em>yyyy-mm-dd</em>.
     *
     * @param value the value. May be {@code null}
     * @return the corresponding date, or {@code null} if {@code value} is null
     */
    public static Date getDate(String value) {
        return value != null ? getDatetime(value + " 0:0:0") : null;
    }

    private static int nextId() {
        return Math.abs(random.nextInt());
    }

}
