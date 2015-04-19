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
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;


/**
 * Tests the {@link IMObjectCopier} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class IMObjectCopierTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link IMObjectCopier#apply method.
     */
    @Test
    public void testApply() {
        String description = "MALE BLACK PUG";
        IMObjectCopier copier
                = new IMObjectCopier(new DefaultIMObjectCopyHandler());
        IMObjectBean bean = createBean("party.animalpet");
        bean.setValue("name", "Fido");
        bean.setValue("sex", "MALE");
        bean.setValue("colour", "BLACK");
        bean.setValue("breed", "PUG");

        // verify object description not set.
        assertNull(bean.getObject().getDescription());

        // verify description is derived when accessed via node.
        assertEquals(description, bean.getValue("description"));

        List<IMObject> objects = copier.apply(bean.getObject());
        assertEquals(1, objects.size());
        IMObject copy = objects.get(0);
        assertTrue(copy != bean.getObject());
        assertEquals("Fido", copy.getName());

        // verify the description in the copy has been derived
        assertEquals(description, copy.getDescription());
    }

    /**
     * Verifies that child objects are copied.
     */
    @Test
    public void testCopyChildren() {
        IMObjectCopier copier
                = new IMObjectCopier(new DefaultIMObjectCopyHandler());
        Party party = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("title", "MR");
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        bean.save();

        Participation participation
                = (Participation) create("participation.customer");
        participation.setEntity(party.getObjectReference());

        // copy the participation and its child party.customerperson
        List<IMObject> objects = copier.apply(participation);

        // objects should contain a participation and party
        assertEquals(2, objects.size());
        assertTrue(TypeHelper.isA(objects.get(0), "participation.customer"));
        assertTrue(TypeHelper.isA(objects.get(1), "party.customerperson"));

        // make sure both objects were copied
        Participation participationCopy = (Participation) objects.get(0);
        Party partyCopy = (Party) objects.get(1);
        assertTrue(participationCopy != participation);
        assertTrue(partyCopy != party);

        // verify the participation references the copy
        assertEquals(partyCopy.getObjectReference(),
                     participationCopy.getEntity());
    }

    /**
     * Verifies that collections are handled correctly when two nodes reference the same collection.
     * This verifies the fix for OVPMS-889.
     */
    @Test
    public void testCopyCollection() {
        IArchetypeService service = (IArchetypeService) applicationContext.getBean("archetypeService");
        Lookup canine = LookupUtil.createLookup(service, "lookup.species", "CANINE");

        IMObjectCopier copier = new IMObjectCopier(new DefaultIMObjectCopyHandler());
        Party pet = (Party) create("party.patientpet");
        IMObjectBean petBean = new IMObjectBean(pet);
        petBean.setValue("name", "Fido");
        petBean.setValue("species", canine.getCode());

        petBean.save();

        Party customer = (Party) create("party.customerperson");
        EntityBean customerBean = new EntityBean(customer);

        customerBean.setValue("title", "MR");
        customerBean.setValue("firstName", "Foo");
        customerBean.setValue("lastName", "Bar");
        customerBean.save();

        customerBean.addRelationship("entityRelationship.patientOwner", pet);
        customerBean.save();
        petBean.save();

        // verify that the customer's 'patientRelationships' and 'sourceRelationships' nodes have identical contents
        List<EntityRelationship> patientRelationships = customerBean.getNodeRelationships("patients");
        List<EntityRelationship> sourceRelationships = customerBean.getNodeRelationships("sourceRelationships");
        assertEquals(1, patientRelationships.size());
        assertEquals(1, sourceRelationships.size());
        assertEquals(patientRelationships, sourceRelationships);

        assertEquals(1, pet.getEntityRelationships().size());

        // now copy the customer and related pet
        List<IMObject> objects = copier.apply(customer);

        // make sure both objects were copied
        assertEquals(2, objects.size());
        Party customerCopy = (Party) objects.get(0);
        Party petCopy = (Party) objects.get(1);

        assertTrue(TypeHelper.isA(customerCopy, "party.customerperson"));
        assertTrue(TypeHelper.isA(petCopy, "party.patientpet"));

        // verify relationships were copied correctly
        assertEquals(1, customerCopy.getEntityRelationships().size());
        assertEquals(1, petCopy.getEntityRelationships().size());
    }

    /**
     * Tests copying.
     */
    @SuppressWarnings({"deprecation"})
    @Test
    public void testCopy() {
        IMObjectCopier copier
                = new IMObjectCopier(new DefaultIMObjectCopyHandler());
        IMObjectBean bean = createBean("party.animalpet");
        bean.setValue("name", "Fido");

        IMObject copy = copier.copy(bean.getObject());
        assertTrue(copy != bean.getObject());
        assertEquals("Fido", copy.getName());
    }
}
