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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


/**
 * Tests the {@link IMObjectCopier} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectCopierTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Tests the {@link IMObjectCopier#apply method.
     */
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
     * Tests copying.
     */
    @SuppressWarnings({"deprecation"})
    public void testCopy() {
        IMObjectCopier copier
                = new IMObjectCopier(new DefaultIMObjectCopyHandler());
        IMObjectBean bean = createBean("party.animalpet");
        bean.setValue("name", "Fido");

        IMObject copy = copier.copy(bean.getObject());
        assertTrue(copy != bean.getObject());
        assertEquals("Fido", copy.getName());
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
     * Helper to create an object and wrap it in an {@link IMObjectBean}.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <code>shortName</code>.
     */
    private IMObjectBean createBean(String shortName) {
        return new IMObjectBean(create(shortName));
    }

}
