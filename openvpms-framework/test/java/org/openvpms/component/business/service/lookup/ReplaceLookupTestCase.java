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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.lookup;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


/**
 * Tests lookup replacement via the {@link ILookupService#replace} method.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("lookup-replace-delete-appcontext.xml")
public class ReplaceLookupTestCase extends AbstractArchetypeServiceTest {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookupService;


    /**
     * Verifies that an entity that has a node that refers to a lookup by its code can have the lookup code replaced.
     */
    @Test public void testEntityCodeReplace() {
        Party party = (Party) create("party.basic");
        checkCodeReplace(party, "description", "lookup.description", false);
        checkCodeReplace(party, "title", "lookup.title", true);
    }

    /**
     * Verifies that an entity that has a classifications node can have the lookup replaced.
     */
    @Test public void testEntityClassificationReplace() {
        Party party = (Party) create("party.basic");
        checkClassificationReplace(party, "classifications", "lookup.category");
    }

    /**
     * Verifies that an act that has a node that refers to a lookup by its code can have the lookup code replaced.
     */
    @Test public void testActCodeReplace() {
        Act act = (Act) create("act.basic");
        checkCodeReplace(act, "status", "lookup.status", false);
        checkCodeReplace(act, "category", "lookup.category", true);
    }

    /**
     * Verifies that an act relationship that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testActRelationshipCodeReplace() {
        Act act1 = (Act) create("act.basic");
        Act act2 = (Act) create("act.basic");
        ActBean bean = new ActBean(act1);
        ActRelationship relationship = bean.addRelationship("actRelationship.basic", act2);
        save(act1, act2);

        checkCodeReplace(relationship, "description", "lookup.description", false);
        checkCodeReplace(relationship, "category", "lookup.category", true);
    }

    /**
     * Verifies that a contact that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testContactCodeReplace() {
        Contact contact = (Contact) create("contact.location");
        checkCodeReplace(contact, "description", "lookup.description", false);

        // Now check the suburb node, which is handled internally by the /details map and contact_details table
        // NOTE: in this instance, the archetype definition restricts the suburbs by the state node but no validation
        // is used to enforce this. It does however demonstrate that nodes with 'targetLookup' lookup assertions are
        // processed correctly.
        checkCodeReplace(contact, "suburb", "lookup.suburb", true);
    }

    /**
     * Verifies that a contact that has a classifications node can have the lookup replaced.
     */
    @Test public void testContactClassificationReplace() {
        Contact contact = (Contact) create("contact.location");
        checkClassificationReplace(contact, "classifications", "lookup.category");
    }

    /**
     * Verifies that a document that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testDocumentCodeReplace() {
        Document document = (Document) create("document.basic");
        checkCodeReplace(document, "mimeType", "lookup.category", false);
        checkCodeReplace(document, "title", "lookup.title", true);
    }

    /**
     * Verifies that an entity identity that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testEntityIdentityCodeReplace() {
        EntityIdentity identity = (EntityIdentity) create("entityIdentity.code");
        checkCodeReplace(identity, "description", "lookup.description", false);
        checkCodeReplace(identity, "status", "lookup.status", true);
    }

    /**
     * Verifies that an entity relationship that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testEntityRelationshipCodeReplace() {
        Party party1 = (Party) create("party.basic");
        Party party2 = (Party) create("party.basic");
        EntityBean bean = new EntityBean(party1);
        EntityRelationship relationship = bean.addRelationship("entityRelationship.basic", party2);
        save(party1, party2);

        checkCodeReplace(relationship, "description", "lookup.description", false);
        checkCodeReplace(relationship, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testLookupCodeReplace() {
        Lookup lookup = createLookup("lookup.staff", "STAFF1");
        checkCodeReplace(lookup, "description", "lookup.description", false);
        checkCodeReplace(lookup, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup relationship that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testLookupRelationshipCodeReplace() {
        Lookup state = createLookup("lookup.state", "VIC");
        Lookup suburb = createLookup("lookup.suburb", "CAPE_WOOLAMAI");
        LookupRelationship relationship = LookupUtil.addRelationship(getArchetypeService(),
                                                                     "lookupRelationship.stateSuburb", state, suburb);
        save(state, suburb);
        checkCodeReplace(relationship, "description", "lookup.description", true);
        // lookup relationship has no top-level nodes suitable for lookup codes.

    }

    /**
     * Verifies that a participation that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testParticipationCodeReplace() {
        Act act = (Act) create("act.basic");
        Party party = (Party) create("party.basic");
        ActBean bean = new ActBean(act);
        Participation participation = bean.addParticipation("participation.basic", party);
        save(act, party);
        checkCodeReplace(participation, "description", "lookup.description", false);
        checkCodeReplace(participation, "category", "lookup.category", true);
    }

    /**
     * Verifies that a product price that has a node that refers to a lookup by its code can have the lookup code
     * replaced.
     */
    @Test public void testProductPriceCodeReplace() {
        ProductPrice price = (ProductPrice) create("productPrice.basic");
        checkCodeReplace(price, "description", "lookup.description", false);
        checkCodeReplace(price, "category", "lookup.category", true);
    }

    /**
     * Verifies that a product price that has a classifications node can have the lookup replaced.
     */
    @Test public void testProductPriceClassificationReplace() {
        ProductPrice price = (ProductPrice) create("productPrice.basic");
        checkClassificationReplace(price, "classifications", "lookup.category");
    }

    /**
     * Verifies that an object that has a node that refers to a lookup via its code, can have the lookup replaced.
     * <p/>
     * The <tt>isDetailsNode</tt> argument is used to indicate nodes that are handled internally via the
     * {@link org.openvpms.component.business.domain.im.common.IMObject#getDetails()} map. These are mapped to a
     * '*_details' table by hibernate and are handled differently by the replacement code.
     *
     * @param object        the object
     * @param node          the lookup code node
     * @param lookup        the lookup archetype short name
     * @param isDetailsNode determines if the node is a 'details' node.
     */
    private void checkCodeReplace(IMObject object, String node, String lookup, boolean isDetailsNode) {
        save(object);
        ArchetypeDescriptor archetype = getArchetypeService().getArchetypeDescriptor(object.getArchetypeId());
        assertNotNull(archetype);
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        assertNotNull(descriptor);
        assertEquals(isDetailsNode, descriptor.getPath().startsWith("/details/"));

        Lookup lookup1 = createLookup(lookup, "CODE1");
        Lookup lookup2 = createLookup(lookup, "CODE2");
        save(lookup1, lookup2);

        IMObjectBean bean = new IMObjectBean(object);
        bean.setValue(node, lookup1.getCode());
        bean.save();

        lookupService.replace(lookup1, lookup2);

        object = get(object);
        bean = new IMObjectBean(object);
        assertEquals(lookup2.getCode(), bean.getValue(node));
    }

    /**
     * Verifies that an object that has a classifications node can have the lookup replaced.
     *
     * @param object         the object
     * @param node           the classification node
     * @param classification the classification lookup archetype short name
     */
    private void checkClassificationReplace(IMObject object, String node, String classification) {
        Lookup class1 = createLookup(classification, "CLASS_1");
        Lookup class2 = createLookup(classification, "CLASS_2");
        Lookup class3 = createLookup(classification, "CLASS_3");
        save(class1, class2, class3);

        // add classification 'CLASS_1'
        IMObjectBean bean = new IMObjectBean(object);
        bean.addValue(node, class1);
        bean.save();

        // replace 'CLASS_1' with 'CLASS_2'
        lookupService.replace(class1, class2);

        // verify it has been replaced
        bean = new IMObjectBean(get(object));
        assertEquals(1, bean.getValues(node).size());
        assertTrue(bean.getValues(node).contains(class2));
        assertFalse(bean.getValues(node).contains(class1));

        // add 'CLASS_3'. The object will now have 2 classifications, 'CLASS_1' and 'CLASS_3'
        bean.addValue(node, class3);
        bean.save();

        // now replace 'CLASS_2' with 'CLASS_3'. As duplicates aren't allowed, there will only be a single
        // 'CLASS_3' on replacement.
        lookupService.replace(class2, class3);

        // verify it has been replaced
        bean = new IMObjectBean(get(object));
        assertEquals(1, bean.getValues(node).size());
        assertTrue(bean.getValues(node).contains(class3));
        assertFalse(bean.getValues(node).contains(class2));
    }

    /**
     * Helper to create a lookup.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code) {
        return LookupUtil.createLookup(getArchetypeService(), shortName, code);
    }


}
