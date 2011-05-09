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

package org.openvpms.archetype.test;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
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


/**
 * Unit test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TestHelper extends Assert {

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
     * @param save      if <code>true</code> make the customer persistent
     * @return a new customer
     */
    public static Party createCustomer(String firstName, String lastName,
                                       boolean save) {
        Party customer = (Party) create("party.customerperson");
        PartyRules rules = new PartyRules();
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
     * @param save if <code>true</code> make the customer persistent
     * @return a new customer
     */
    public static Party createCustomer(boolean save) {

        return createCustomer("J", "Zoo-" + System.currentTimeMillis(), save);
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
        Lookup state = getLookup("lookup.state", stateCode);
        Lookup suburb = getLookup("lookup.suburb", suburbCode, state, "lookupRelationship.stateSuburb");
        Contact contact = (Contact) create(ContactArchetypes.LOCATION);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("address", address);
        bean.setValue("suburb", suburb.getCode());
        bean.setValue("state", state.getCode());
        bean.setValue("postcode", postCode);
        return contact;
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
     * @param save if <code>true</code> make the patient persistent
     * @return a new patient
     */
    public static Party createPatient(boolean save) {
        Party patient = (Party) create("party.patientpet");
        EntityBean bean = new EntityBean(patient);
        bean.setValue("name", "XPatient-" + System.currentTimeMillis());
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
     * @param save  if <code>true</code>, make the patient persistent
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
     * Creates and saves a new user.
     *
     * @return a new user
     */
    public static User createUser() {
        return createUser("zuser" + System.currentTimeMillis(), true);
    }

    /**
     * Creates a new user.
     *
     * @param username the login name
     * @param save     if <code>true</code> make the user persistent
     * @return a new user
     */
    public static User createUser(String username, boolean save) {
        User user = (User) create("security.user");
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
     * @param save if <code>true</code> make the user persistent
     * @return a new user
     */
    public static User createClinician(boolean save) {
        User user = createUser("zvet" + System.currentTimeMillis(), false);
        user.addClassification(
                getLookup("lookup.userType", "CLINICIAN"));
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
     * @param species the species classification. May be <code>null</code>
     * @return a new product
     */
    public static Product createProduct(String species) {
        return createProduct("product.medication", species);
    }

    /**
     * Creates and saves a new product with an optional species classification.
     * The product name is prefixed with <em>XProduct-</em>.
     *
     * @param shortName the archetype short name
     * @param species   the species classification name. May be <code>null</code>
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
     * @param species   the species classification name. May be <tt>null</tt>
     * @param save      if <tt>true</tt>, save the product
     * @return a new product
     */
    public static Product createProduct(String shortName, String species,
                                        boolean save) {
        Product product = (Product) create(shortName);
        EntityBean bean = new EntityBean(product);
        String name = "XProduct-" + ((species != null) ? species : "")
                      + System.currentTimeMillis();
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
     * Creates and saves new <code>party.supplierorganisation</em>.
     *
     * @return a new party
     */
    public static Party createSupplier() {
        return createSupplier(true);
    }

    /**
     * Creates a new <code>party.supplierorganisation</em>.
     *
     * @param save if <tt>true</tt> save the supplier
     * @return a new party
     */
    public static Party createSupplier(boolean save) {
        Party party = (Party) create("party.supplierorganisation");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("name", "XSupplier");
        if (save) {
            bean.save();
        }
        return party;
    }

    /**
     * Creates a new <code>party.supplierVeterinarian</em>.
     *
     * @return a new party
     */
    public static Party createSupplierVet() {
        Party party = (Party) create("party.supplierVeterinarian");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "XSupplierVet");
        bean.setValue("title", "MR");
        bean.save();
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
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE,
                                                  true, true);
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

        PartyRules rules = new PartyRules();
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
     * Creates a new <tt>party.organisationLocation</tt>.
     *
     * @return a new location
     */
    public static Party createLocation() {
        Party party = (Party) create(PracticeArchetypes.LOCATION);
        party.setName("XLocation");
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        party.addContact(contact);
        save(party);
        return party;
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
     * @param save      if <tt>true</tt>, save the lookup
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
     * @param save      if <tt>true</tt>, save the lookup
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
     * Returns a lookup that is the target in a lookup relationship, creating
     * and saving it if it doesn't exist.
     *
     * @param shortName             the target lookup short name
     * @param code                  the lookup code
     * @param source                the source lookup
     * @param relationshipShortName the lookup relationship short name
     * @return the lookup
     */
    public static Lookup getLookup(String shortName, String code, Lookup source,
                                   String relationshipShortName) {
        Lookup target = getLookup(shortName, code);
        for (LookupRelationship relationship
                : source.getLookupRelationships()) {
            if (relationship.getTarget().equals(target.getObjectReference())) {
                return target;
            }
        }
        LookupRelationship relationship
                = (LookupRelationship) create(relationshipShortName);
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
     * @return the corresponding lookup's name. May be <tt>null</tt>
     */
    public static String getLookupName(IMObject object, String node) {
        IMObjectBean bean = new IMObjectBean(object);
        return LookupHelper.getName(
                ArchetypeServiceHelper.getArchetypeService(),
                bean.getDescriptor(node),
                object);
    }

    /**
     * Helper to create a date-time given a string of the form
     * <em>yyyy-mm-dd hh:mm:ss</em>.
     *
     * @param value the value
     * @return the corresponding date-time
     */
    public static Date getDatetime(String value) {
        return new Date(Timestamp.valueOf(value).getTime()); // use Date, for easy comparison
    }

    /**
     * Helper to create a date given a string of the form <em>yyyy-mm-dd</em>.
     *
     * @param value the value
     * @return the corresponding date
     */
    public static Date getDate(String value) {
        return getDatetime(value + " 0:0:0");
    }

}
