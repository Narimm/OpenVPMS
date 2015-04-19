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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.test.context.ContextConfiguration;


/**
 * Verifies that lookup instances can only be deleted when they aren't referred to by another object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("lookup-replace-delete-appcontext.xml")
public class DeleteLookupTestCase extends AbstractArchetypeServiceTest {


    /**
     * Verifies that a lookup that is referred to by an entity via its code can only be deleted once the entity
     * no longer refers to it.
     */
    @Test public void testEntityCodeDelete() {
        Party party = (Party) create("party.basic");
        checkCodeDelete(party, "description", "lookup.description", false);
        checkCodeDelete(party, "title", "lookup.title", true);
    }

    /**
     * Verifies that a lookup that is referred to by an entity as a classification can only be deleted once the entity
     * no longer refers to it.
     */
    @Test public void testEntityClassificationDelete() {
        Party party = (Party) create("party.basic");
        checkClassificationDelete(party, "classifications", "lookup.category");
    }

    /**
     * Verifies that a lookup that is referred to by an act via its code can only be deleted once the act no longer
     * refers to it.
     */
    @Test public void testActCodeDelete() {
        Act act = (Act) create("act.basic");
        checkCodeDelete(act, "status", "lookup.status", false);
        checkCodeDelete(act, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup that is referred to by an act relationship via its code can only be deleted once the
     * act relationship no longer refers to it.
     */
    @Test public void testActRelationshipCodeDelete() {
        Act act1 = (Act) create("act.basic");
        Act act2 = (Act) create("act.basic");
        ActBean bean = new ActBean(act1);
        ActRelationship relationship = bean.addRelationship("actRelationship.basic", act2);
        save(act1, act2);

        checkCodeDelete(relationship, "description", "lookup.description", false);
        checkCodeDelete(relationship, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup that is referred to by a contact via its code can only be deleted once the contact no
     * longer refers to it.
     */
    @Test public void testContactCodeDelete() {
        Contact contact = (Contact) create("contact.location");
        checkCodeDelete(contact, "description", "lookup.description", false);

        // Now check the suburb node, which is handled internally by the /details map and contact_details table
        // NOTE: in this instance, the archetype definition restricts the suburbs by the state node but no validation
        // is used to enforce this. It does however demonstrate that nodes with 'targetLookup' lookup assertions are
        // processed correctly.
        checkCodeDelete(contact, "suburb", "lookup.suburb", true);
    }

    /**
     * Verifies that a lookup that is referred to by an contact as a classification can only be deleted once the contact
     * no longer refers to it.
     */
    @Test public void testContactClassificationDelete() {
        Contact contact = (Contact) create("contact.location");
        checkClassificationDelete(contact, "classifications", "lookup.category");
    }

    /**
     * Verifies that a lookup that is referred to by a document via its code can only be deleted once the document no
     * longer refers to it.
     */
    @Test public void testDocumentCodeDelete() {
        Document document = (Document) create("document.basic");
        checkCodeDelete(document, "mimeType", "lookup.category", false);
        checkCodeDelete(document, "title", "lookup.title", true);
    }

    /**
     * Verifies that a lookup that is referred to by an entity identity via its code can only be deleted once the
     * entity identity no longer refers to it.
     */
    @Test public void testEntityIdentityCodeDelete() {
        EntityIdentity identity = (EntityIdentity) create("entityIdentity.code");
        checkCodeDelete(identity, "description", "lookup.description", false);
        checkCodeDelete(identity, "status", "lookup.status", true);
    }

    /**
     * Verifies that a lookup that is referred to by an entity relationship via its code can only be deleted once the
     * entity relationship no longer refers to it.
     */
    @Test public void testEntityRelationshipCodeDelete() {
        Party party1 = (Party) create("party.basic");
        Party party2 = (Party) create("party.basic");
        EntityBean bean = new EntityBean(party1);
        EntityRelationship relationship = bean.addRelationship("entityRelationship.basic", party2);
        save(party1, party2);

        checkCodeDelete(relationship, "description", "lookup.description", false);
        checkCodeDelete(relationship, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup that is referred to by another lookup via its code can only be deleted once the
     * lookup no longer refers to it.
     */
    @Test public void testLookupCodeDelete() {
        Lookup lookup = createLookup("lookup.staff", "STAFF1");
        checkCodeDelete(lookup, "description", "lookup.description", false);
        checkCodeDelete(lookup, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup that is referred to by a lookup relationship via its code can only be deleted once the
     * lookup relationship no longer refers to it.
     */
    @Test public void testLookupRelationshipCodeDelete() {
        Lookup state = createLookup("lookup.state", "VIC");
        Lookup suburb = createLookup("lookup.suburb", "CAPE_WOOLAMAI");
        LookupRelationship relationship = LookupUtil.addRelationship(getArchetypeService(),
                                                                     "lookupRelationship.stateSuburb", state, suburb);
        save(state, suburb);
        checkCodeDelete(relationship, "description", "lookup.description", true);
        // lookup relationship has no top-level nodes suitable for lookup codes.
    }

    /**
     * Verifies that a lookup that is referred to by a participation via its code can only be deleted once the
     * participation no longer refers to it.
     */
    @Test public void testParticipationCodeDelete() {
        Act act = (Act) create("act.basic");
        Party party = (Party) create("party.basic");
        ActBean bean = new ActBean(act);
        Participation participation = bean.addParticipation("participation.basic", party);
        save(act, party);
        checkCodeDelete(participation, "description", "lookup.description", false);
        checkCodeDelete(participation, "category", "lookup.category", true);
    }

    /**
     * Verifies that a product price that is referred to by a participation via its code can only be deleted once the
     * product price no longer refers to it.
     */
    @Test public void testProductPriceCodeDelete() {
        ProductPrice price = (ProductPrice) create("productPrice.basic");
        checkCodeDelete(price, "description", "lookup.description", false);
        checkCodeDelete(price, "category", "lookup.category", true);
    }

    /**
     * Verifies that a lookup that is referred to by an product price as a classification can only be deleted once the
     * product price no longer refers to it.
     */
    @Test public void testProductPriceClassificationDelete() {
        ProductPrice price = (ProductPrice) create("productPrice.basic");
        checkClassificationDelete(price, "classifications", "lookup.category");
    }

    /**
     * Verifies that a lookup that is referred to by an object via its code can only be deleted once the object
     * no longer refers to it.
     * <p/>
     * The <tt>isDetailsNode</tt> argument is used to indicate nodes that are handled internally via the
     * {@link org.openvpms.component.business.domain.im.common.IMObject#getDetails()} map. These are mapped to a
     * '*_details' table by hibernate and are handled differently by the usage code.
     *
     * @param object        the object
     * @param node          the lookup code node
     * @param lookup        the lookup archetype short name
     * @param isDetailsNode determines if the node is a 'details' node.
     */
    private void checkCodeDelete(IMObject object, String node, String lookup, boolean isDetailsNode) {
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

        try {
            remove(lookup1);
            fail("Expected an ArchetypeServiceException to be thrown");
        } catch (ArchetypeServiceException exception) {
            assertEquals(ArchetypeServiceException.ErrorCode.CannotDeleteLookupInUse, exception.getErrorCode());
        }
        bean.setValue(node, lookup2.getCode());
        bean.save();
        remove(lookup1); // should now be able to remove the lookup
    }

    /**
     * Verifies that a lookup that is referred to by an object as a classification can only be deleted once the object
     * no longer refers to it.
     *
     * @param object         the object
     * @param node           the classification node
     * @param classification the classification lookup archetype short name
     */
    private void checkClassificationDelete(IMObject object, String node, String classification) {
        Lookup class1 = createLookup(classification, "CLASS_1");
        Lookup class2 = createLookup(classification, "CLASS_2");
        save(class1, class2);

        // add classification 'CLASS_1'
        IMObjectBean bean = new IMObjectBean(object);
        bean.addValue(node, class1);
        bean.save();

        try {
            remove(class1);
            fail("Expected an ArchetypeServiceException to be thrown");
        } catch (ArchetypeServiceException exception) {
            assertEquals(ArchetypeServiceException.ErrorCode.CannotDeleteLookupInUse, exception.getErrorCode());
        }
        bean.removeValue(node, class1);
        bean.addValue(node, class2);
        bean.save();
        remove(class1); // should now be able to remove the lookup
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