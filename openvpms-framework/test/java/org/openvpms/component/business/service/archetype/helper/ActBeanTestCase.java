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

package org.openvpms.component.business.service.archetype.helper;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;


/**
 * Tests the {@link ActBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class ActBeanTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link ActBean#addRelationship},
     * {@link ActBean#getRelationship)}, {@link ActBean#getRelationship(String)},
     * {@link ActBean#hasRelationship(String)}, {@link ActBean#getRelationships} and {@link ActBean#hasRelationship}
     * methods.
     */
    @Test
    public void testRelationships() {
        final String relName = "actRelationship.customerEstimationItem";
        Act target = (Act) create("act.customerEstimationItem");
        ActBean bean = createActBean("act.customerEstimation");
        Act source = bean.getAct();
        assertNull(bean.getRelationship(target));
        assertEquals(0, bean.getRelationships(relName).size());

        ActRelationship r = bean.addRelationship(relName, target);
        assertTrue(target.getActRelationships().contains(r));

        assertEquals(r, bean.getRelationship(relName));
        assertNull(bean.getRelationship("foo"));
        assertTrue(bean.hasRelationship(relName));
        assertFalse(bean.hasRelationship("foo"));
        assertTrue(bean.hasRelationship(relName, target));
        assertFalse(bean.hasRelationship("foo", target));

        checkRelationship(r, relName, source, target);
        r = bean.getRelationship(target);
        checkRelationship(r, relName, source, target);

        assertEquals(1, bean.getRelationships(relName).size());

        bean.removeRelationship(r);
        assertNull(bean.getRelationship(target));
        assertNull(bean.getRelationship(relName));
        assertFalse(bean.hasRelationship(relName));
        assertFalse(bean.hasRelationship(relName, target));
        assertEquals(0, bean.getRelationships(relName).size());
    }

    /**
     * Tests the {@link ActBean#getActs} method.
     */
    @Test
    public void testGetActs() {
        final String relName = "actRelationship.customerEstimationItem";
        ActBean bean = createActBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            save(target);
            bean.addRelationship(relName, target);
            expected[i] = target;
        }
        List<Act> acts = bean.getActs();
        assertEquals(expected.length, acts.size());
        for (Act exp : expected) {
            assertTrue(acts.contains(exp));
        }
    }

    /**
     * Tests the {@link ActBean#getActs(String)} method.
     */
    @Test
    public void testGetActsByShortName() {
        final String relName = "actRelationship.customerEstimationItem";
        ActBean bean = createActBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            save(target);
            bean.addRelationship(relName, target);
            expected[i] = target;
        }
        List<Act> acts = bean.getActs("act.customerEstimationItem");
        assertEquals(expected.length, acts.size());
        for (Act exp : expected) {
            assertTrue(acts.contains(exp));
        }

        // test wildcards
        acts = bean.getActs("act.customerEstimation*");
        assertEquals(expected.length, acts.size());
        for (Act exp : expected) {
            assertTrue(acts.contains(exp));
        }

        // test no match
        acts = bean.getActs("act.customerAccountInvoiceItem");
        assertTrue(acts.isEmpty());
    }

    /**
     * Tests the {@link ActBean#getNodeActs} method.
     */
    @Test
    public void testGetNodeActs() {
        final String relName = "actRelationship.customerEstimationItem";
        ActBean bean = createActBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            save(target);
            bean.addRelationship(relName, target);
            expected[i] = target;
        }
        List<Act> acts = bean.getNodeActs("items");
        assertEquals(expected.length, acts.size());
        for (Act exp : expected) {
            assertTrue(acts.contains(exp));
        }
    }

    /**
     * Tests the {@link ActBean#addNodeRelationship} method.
     */
    @Test
    public void testAddNodeRelationship() {
        ActBean bean = createActBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            expected[i] = target;
            save(target);

            ActRelationship r = bean.addNodeRelationship("items", target);
            assertNotNull(r);
            assertEquals(bean.getReference(), r.getSource());
            assertEquals(target.getObjectReference(), r.getTarget());
        }
        List<Act> acts = bean.getNodeActs("items");
        assertEquals(expected.length, acts.size());
        for (Act exp : expected) {
            assertTrue(acts.contains(exp));
        }

        try {
            Act target = (Act) create("act.customerEstimation");
            bean.addNodeRelationship("items", target);
            fail("Expected addNodeRelationship() to fail");
        } catch (IMObjectBeanException exception) {
            assertEquals(IMObjectBeanException.ErrorCode.CannotAddTargetToNode, exception.getErrorCode());
        }
    }

    /**
     * Tests the {@link ActBean#addParticipation),
     * {@link ActBean#getParticipation(String),
     * {@link ActBean#removeParticipation} and
     * {@link ActBean#setParticipant} methods.
     */
    @Test
    public void testParticipations() {
        final String pName = "participation.customer";
        ActBean bean = createActBean("act.customerEstimation");
        Party customer1 = createCustomer();
        ArchetypeServiceHelper.getArchetypeService().save(customer1);
        Party customer2 = createCustomer();

        assertNull(bean.getParticipation(pName));

        // add a participation and verify it can be retrieved
        Participation p = bean.addParticipation(pName, customer1);
        checkParticipation(p, pName, customer1);
        p = bean.getParticipation(pName);
        checkParticipation(p, pName, customer1);

        //  test getParticipant
        Entity e = bean.getParticipant(pName);
        assertNotNull(e);
        assertEquals(customer1, e);

        // remove the participation and verify it has been removed
        p = bean.removeParticipation(pName);
        checkParticipation(p, pName, customer1);
        assertNull(bean.getParticipation(pName));

        // test setParticipant
        p = bean.setParticipant(pName, customer1);
        checkParticipation(p, pName, customer1);
        p = bean.setParticipant(pName, customer2);
        checkParticipation(p, pName, customer2);
        p = bean.getParticipation(pName);
        checkParticipation(p, pName, customer2);
    }

    /**
     * Tests the {@link ActBean#getNodeParticipant(String)} and
     * {@link ActBean#getNodeParticipantRef(String)} method.
     */
    @Test
    public void testGetNodeParticipant() {
        ActBean bean = createActBean("act.customerEstimation");
        Party customer = createCustomer();
        save(customer);

        assertNull(bean.getNodeParticipantRef("customer"));
        assertNull(bean.getNodeParticipant("customer"));

        bean.addParticipation("participation.customer", customer);
        assertEquals(customer.getObjectReference(),
                     bean.getNodeParticipantRef("customer"));

        assertEquals(customer, bean.getNodeParticipant("customer"));
    }

    /**
     * Tests the {@link ActBean#addNodeParticipation} method.
     */
    @Test
    public void testAddNodeParticipation() {
        ActBean bean = createActBean("act.customerEstimation");
        Party customer = createCustomer();
        Participation participation = bean.addNodeParticipation("customer", customer);
        assertNotNull(participation);
        assertEquals(bean.getReference(), participation.getAct());
        assertEquals(customer.getObjectReference(), participation.getEntity());

        Party patient = (Party) create("party.patientpet");

        try {
            bean.addNodeParticipation("customer", patient);
            fail("Expected addNodeParticipation() to fail");
        } catch (IMObjectBeanException exception) {
            assertEquals(IMObjectBeanException.ErrorCode.CannotAddTargetToNode, exception.getErrorCode());
        }
    }

    /**
     * Verifies that an act relationship matches that expected.
     *
     * @param relationship the relationship
     * @param shortName    the expected short name
     * @param source       the expected source act
     * @param target       the expected target act
     */
    private void checkRelationship(ActRelationship relationship,
                                   String shortName, Act source, Act target) {
        assertNotNull(relationship);
        assertEquals(shortName, relationship.getArchetypeId().getShortName());
        assertEquals(source.getObjectReference(), relationship.getSource());
        assertEquals(target.getObjectReference(), relationship.getTarget());
    }

    /**
     * Verifies that a participation matches that expected.
     *
     * @param participation the participation
     * @param shortName     the expected short name
     * @param entity        the expected entity
     */
    private void checkParticipation(Participation participation,
                                    String shortName, Entity entity) {
        assertNotNull(participation);
        assertEquals(shortName, participation.getArchetypeId().getShortName());
        assertEquals(entity.getObjectReference(), participation.getEntity());
    }

    /**
     * Helper to create an act and wrap it in an {@link ActBean}.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <tt>shortName</tt>.
     */
    private ActBean createActBean(String shortName) {
        Act object = (Act) create(shortName);
        return new ActBean(object);
    }

    /**
     * Helper to create a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        Party party = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("title", "MR");
        bean.setValue("firstName", "ZFoo");
        bean.setValue("lastName", "ZActBeanTestCase.Customer" + party.hashCode());
        return party;
    }

}
