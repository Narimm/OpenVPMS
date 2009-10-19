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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


/**
 * Tests the {@link ActBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActBeanTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Tests the {@link ActBean#addRelationship},
     * {@link ActBean#getRelationship)} {@link ActBean#getRelationships}
     * and {@link ActBean#hasRelationship} methods.
     */
    public void testRelationships() {
        final String relName = "actRelationship.customerEstimationItem";
        Act target = (Act) create("act.customerEstimationItem");
        ActBean bean = createBean("act.customerEstimation");
        Act source = bean.getAct();
        assertNull(bean.getRelationship(target));
        assertEquals(0, bean.getRelationships(relName).size());

        ActRelationship r = bean.addRelationship(relName, target);
        assertTrue(target.getActRelationships().contains(r));
        assertTrue(bean.hasRelationship(relName, target));
        assertFalse(bean.hasRelationship("foo", target));

        checkRelationship(r, relName, source, target);
        r = bean.getRelationship(target);
        checkRelationship(r, relName, source, target);

        assertEquals(1, bean.getRelationships(relName).size());

        bean.removeRelationship(r);
        assertNull(bean.getRelationship(target));
        assertFalse(bean.hasRelationship(relName, target));
        assertEquals(0, bean.getRelationships(relName).size());
    }

    /**
     * Tests the {@link ActBean#getActs} method.
     */
    public void testGetActs() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        final String relName = "actRelationship.customerEstimationItem";
        ActBean bean = createBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            service.save(target);
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
    public void testGetActsByShortName() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        final String relName = "actRelationship.customerEstimationItem";
        ActBean bean = createBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            service.save(target);
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
    public void testGetNodeActs() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        final String relName = "actRelationship.customerEstimationItem";
        ActBean bean = createBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            service.save(target);
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
    public void testAddNodeRelationship() {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        ActBean bean = createBean("act.customerEstimation");
        Act[] expected = new Act[3];
        for (int i = 0; i < 3; ++i) {
            Act target = (Act) create("act.customerEstimationItem");
            expected[i] = target;
            service.save(target);

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
    public void testParticipations() {
        final String pName = "participation.customer";
        ActBean bean = createBean("act.customerEstimation");
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
    public void testGetNodeParticipant() {
        ActBean bean = createBean("act.customerEstimation");
        Party customer = createCustomer();
        ArchetypeServiceHelper.getArchetypeService().save(customer);

        assertNull(bean.getNodeParticipantRef("customer"));
        assertNull(bean.getNodeParticipant("customer"));

        bean.addParticipation("participation.customer", customer);
        assertEquals(customer.getObjectReference(),
                     bean.getNodeParticipantRef("customer"));

        assertEquals(customer, bean.getNodeParticipant("customer"));
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
     * Helper to create an object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObject create(String shortName) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Helper to create an act and wrap it in an {@link ActBean}.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <code>shortName</code>.
     */
    private ActBean createBean(String shortName) {
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
