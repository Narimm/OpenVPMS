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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Tests the {@link EntityBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityBeanTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Tests the {@link EntityBean#addRelationship} and
     * {@link EntityBean#getRelationship)} methods.
     */
    public void testRelationships() {
        final String relName = "entityRelationship.animalOwner";
        Party pet = (Party) create("animal.pet");
        EntityBean bean = createBean("person.person");
        assertNull(bean.getRelationship(pet));

        EntityRelationship r = bean.addRelationship(relName, pet);
        checkRelationship(r, relName, bean.getEntity(), pet);
        r = bean.getRelationship(pet);
        checkRelationship(r, relName, bean.getEntity(), pet);

        bean.removeRelationship(r);
        assertNull(bean.getRelationship(pet));
    }

    /**
     * Tests the {@link ActBean#addParticipation),
     * {@link EntityBean#getParticipation(String),
     * {@link EntityBean#removeParticipation} and
     * {@link EntityBean#setParticipant} methods.
     */
    public void testParticipations() {
        final String pName = "participation.customer";
        Entity entity = (Entity) create("party.customerperson");
        EntityBean bean = new EntityBean(entity);
        Act act1 = (Act) create("act.customerEstimationItem");
        ArchetypeServiceHelper.getArchetypeService().save(act1);
        Act act2 = (Act) create("act.customerEstimationItem");
        ArchetypeServiceHelper.getArchetypeService().save(act2);

        assertNull(bean.getParticipation(pName));

        // add a participation and verify it can be retrieved
        Participation p = bean.addParticipation(pName, act1);
        checkParticipation(p, pName, entity, act1);
        p = bean.getParticipation(pName);
        checkParticipation(p, pName, entity, act1);

        //  test getParticipant
        Act act = bean.getParticipant(pName);
        assertNotNull(act);
        assertEquals(act1, act);

        // remove the participation and verify it has been removed
        p = bean.removeParticipation(pName);
        checkParticipation(p, pName, entity, act1);
        assertNull(bean.getParticipation(pName));

        // test setParticipant
        p = bean.setParticipant(pName, act1);
        checkParticipation(p, pName, entity, act1);
        p = bean.setParticipant(pName, act2);
        checkParticipation(p, pName, entity, act2);
        p = bean.getParticipation(pName);
        checkParticipation(p, pName, entity, act2);
    }

    /**
     * Verifies that an entity relationship matches that expected.
     *
     * @param relationship the relationship
     * @param shortName    the expected short name
     * @param source       the expected source entity
     * @param target       the expected target enttiy
     */
    private void checkRelationship(EntityRelationship relationship,
                                   String shortName, Entity source,
                                   Entity target) {
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
     * @param act           the expected act
     */
    private void checkParticipation(Participation participation,
                                    String shortName, Entity entity,
                                    Act act) {
        assertNotNull(participation);
        assertEquals(shortName, participation.getArchetypeId().getShortName());
        assertEquals(entity.getObjectReference(), participation.getEntity());
        assertEquals(act.getObjectReference(), participation.getAct());
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
     * Helper to create an entity and wrap it in an {@link EntityBean}.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <code>shortName</code>.
     */
    private EntityBean createBean(String shortName) {
        Entity object = (Entity) create(shortName);
        return new EntityBean(object);
    }

}
