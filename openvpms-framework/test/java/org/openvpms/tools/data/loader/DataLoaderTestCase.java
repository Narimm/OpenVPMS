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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.tools.data.loader;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.List;
import java.util.Set;


/**
 * Tests the {@link DataLoader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DataLoaderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    private IArchetypeService service;
    private static final String CUSTOMERS = "/org/openvpms/tools/data/loader/customers.xml";
    private static final String ACTS = "/org/openvpms/tools/data/loader/acts.xml";
    private static final String PRODUCTS = "/org/openvpms/tools/data/loader/products.xml";

    private static final String SECURITY = "/org/openvpms/tools/data/loader/security.xml";

    public void testCustomers() throws Exception {
        DataLoader loader = new DataLoader(10);
        load(loader, CUSTOMERS);
        loader.flush();
        LoadCache cache = loader.getLoadCache();
        Party customer = checkCustomer(cache, "C1", "MR", "Foo", "F", "Bar");
        Party patient = checkPatient(cache, "P1", "Spot", "CANINE",
                                     "GERMAN_SHEPHERD_DOG");
        Set<Contact> contacts = customer.getContacts();
        assertEquals(2, contacts.size());
        checkContact(contacts, "contact.phoneNumber", "(03) 98754312");
        checkContact(contacts, "contact.location",
                     "12 Station Road Black Rock 3456");
        assertEquals(1, customer.getEntityRelationships().size());
        assertEquals(1, patient.getEntityRelationships().size());
        checkRelationship("entityRelationship.patientOwner", customer, patient);

        Lookup lookup = checkLookup(cache, "V1", "lookup.staff", "VET");
        assertEquals(1, customer.getClassifications().size());
        assertTrue(customer.getClassifications().contains(lookup));
    }

    public void testActs() throws Exception {
        DataLoader loader = new DataLoader(10);
        load(loader, CUSTOMERS);
        load(loader, ACTS);
        loader.flush();
        LoadCache cache = loader.getLoadCache();
        Act act = (Act) checkReference(cache, "A1", "act.customerEstimation");
        Act item = (Act) checkReference(cache, "A2",
                                        "act.customerEstimationItem");
        checkActRelationship("actRelationship.customerEstimationItem", act,
                             item);
        Party patient = (Party) checkReference(cache, "P1", "party.patientpet");
        checkParticipation(cache, "PART1", "participation.patient", item,
                           patient);
    }

    public void testProducts() throws Exception {
        DataLoader loader = new DataLoader(10);
        load(loader, PRODUCTS);
        loader.flush();
    }

    public void testSecurity() throws Exception {
        DataLoader loader = new DataLoader(6);
        load(loader, SECURITY);
        loader.flush();
        LoadCache cache = loader.getLoadCache();

        SecurityRole role = (SecurityRole) checkReference(cache, "ROLE1",
                                                          "security.role");

        User user = (User) checkReference(cache, "VET1", "security.user");
        assertEquals("A Vet", user.getName());
        assertEquals("vet", user.getUsername());
        assertEquals("secret", user.getPassword());

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role));
        assertEquals(1, role.getUsers().size());
        assertTrue(role.getUsers().contains(user));

        ArchetypeAwareGrantedAuthority auth1 = (ArchetypeAwareGrantedAuthority)
                checkReference(cache, "AUTH1", "security.archetypeAuthority");
        assertEquals("save.all", auth1.getName());
        assertEquals("archetypeService", auth1.getServiceName());
        assertEquals("Authority to Save All Archetypes",
                     auth1.getDescription());
        assertEquals("save", auth1.getMethod());
        assertEquals("*", auth1.getArchetypeShortName());

        ArchetypeAwareGrantedAuthority auth2 = (ArchetypeAwareGrantedAuthority)
                checkReference(cache, "AUTH2", "security.archetypeAuthority");
        assertEquals("remove.all", auth2.getName());
        assertEquals("archetypeService", auth2.getServiceName());
        assertEquals("Authority to Remove All Archetypes",
                     auth2.getDescription());
        assertEquals("remove", auth2.getMethod());
        assertEquals("*", auth2.getArchetypeShortName());

        assertEquals(2, role.getAuthorities().size());
        assertTrue(role.getAuthorities().contains(auth1));
        assertTrue(role.getAuthorities().contains(auth2));
    }

    private void load(DataLoader loader, String file) throws Exception {
        InputStream stream = getClass().getResourceAsStream(file);
        assertNotNull(stream);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(stream);
        loader.load(reader, file);
    }


    private void checkParticipation(LoadCache cache, String id,
                                    String shortName, Act act,
                                    Entity entity) {
        Participation p = (Participation) checkReference(cache, id, shortName);
        assertEquals(act.getObjectReference(), p.getAct());
        assertEquals(entity.getObjectReference(), p.getEntity());
    }

    private Lookup checkLookup(LoadCache cache, String id,
                               String shortName, String code) {
        Lookup lookup = (Lookup) checkReference(cache, id, shortName);
        assertEquals(code, lookup.getCode());
        return lookup;
    }

    private void checkRelationship(String shortName, Party source,
                                   Party target) {
        EntityRelationship found = null;
        Set<EntityRelationship> relationships
                = source.getEntityRelationships();
        for (EntityRelationship relationship : relationships) {
            if (TypeHelper.isA(relationship, shortName)) {
                found = relationship;
                break;
            }
        }
        assertNotNull(found);
        assertEquals(source.getObjectReference(), found.getSource());
        assertEquals(target.getObjectReference(), found.getTarget());
        assertTrue(target.getEntityRelationships().contains(found));
    }

    private void checkActRelationship(String shortName, Act source,
                                      Act target) {
        ActRelationship found = null;
        Set<ActRelationship> relationships
                = source.getActRelationships();
        for (ActRelationship relationship : relationships) {
            if (TypeHelper.isA(relationship, shortName)) {
                found = relationship;
            }
        }
        assertNotNull(found);
        assertEquals(source.getObjectReference(), found.getSource());
        assertEquals(target.getObjectReference(), found.getTarget());
        assertTrue(target.getActRelationships().contains(found));
    }

    private void checkContact(Set<Contact> contacts, String shortName,
                              String description) {
        Contact found = null;
        for (Contact contact : contacts) {
            if (TypeHelper.isA(contact, shortName)) {
                found = contact;
                break;
            }
        }
        assertNotNull(found);
        assertEquals(description, found.getDescription());
    }

    private Party checkCustomer(LoadCache cache, String id, String title,
                                String firstName, String initials,
                                String lastName) {
        Party customer = (Party) checkReference(cache, id,
                                                "party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        assertEquals(title, bean.getString("title"));
        assertEquals(firstName, bean.getString("firstName"));
        assertEquals(initials, bean.getString("initials"));
        assertEquals(lastName, bean.getString("lastName"));
        String name = lastName + "," + firstName;
        assertEquals(name, bean.getString("name"));
        return customer;
    }

    private Party checkPatient(LoadCache cache, String id, String name,
                               String species, String breed) {
        Party patient = (Party) checkReference(cache, id, "party.patientpet");
        IMObjectBean bean = new IMObjectBean(patient);
        assertEquals(name, bean.getString("name"));
        assertEquals(species, bean.getString("species"));
        assertEquals(breed, bean.getString("breed"));
        return patient;
    }

    private IMObject checkReference(LoadCache cache, String id,
                                    String shortName) {
        IMObjectReference ref = cache.getReference(id);
        assertNotNull(ref);
        assertEquals(shortName, ref.getArchetypeId().getShortName());
        IMObject object = service.get(ref);
        assertNotNull(object);
        return object;
    }

    @Override
    protected void onSetUp() throws Exception {
        service = ArchetypeServiceHelper.getArchetypeService();
        deleteParties("party.customerperson");
        deleteParties("party.person");
        ArchetypeQuery query;
        query = new ArchetypeQuery("lookup.staff", false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> lookups = service.get(query).getResults();
        for (IMObject lookup : lookups) {
            service.remove(lookup);
        }

    }

    /**
     * (non-Javadoc)
     *
     * @see AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /**
     * Deletes parties with the specified short name having lookup.staff
     * classifications.
     *
     * @param shortName the party short name
     */
    private void deleteParties(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false,
                                                  false);
        query.add(new CollectionNodeConstraint("classifications",
                                               "lookup.staff", false, false));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        for (IMObject object : service.get(query).getResults()) {
            service.remove(object);
        }
    }

}
