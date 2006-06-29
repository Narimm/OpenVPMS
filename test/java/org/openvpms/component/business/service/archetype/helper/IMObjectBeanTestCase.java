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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.*;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link IMObjectBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectBeanTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Tests the {@link IMObjectBean#hasNode(String)} method.
     */
    public void testHasNode() {
        IMObjectBean bean = createBean("party.customerperson");
        assertTrue(bean.hasNode("firstName"));
        assertFalse(bean.hasNode("nonode"));
    }

    /**
     * Tests the {@link IMObjectBean#getDescriptor} method.
     */
    public void testGetDescriptor() {
        IMObjectBean bean = createBean("party.customerperson");
        NodeDescriptor node = bean.getDescriptor("firstName");
        assertNotNull(node);
        assertEquals("firstName", node.getName());

        assertNull(bean.getDescriptor("nonode"));
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName()} method.
     */
    public void testGetDisplayName() {
        IMObjectBean pet = createBean("animal.pet");
        assertEquals("Patient(Pet)", pet.getDisplayName());

        // verify shortname is returned when no display name is present
        IMObjectBean customer = createBean("party.customerperson");
        assertEquals("party.customerperson", customer.getDisplayName());
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName(String)} method.
     */
    public void testNodeDisplayName() {
        IMObjectBean act = createBean("act.customerAccountPayment");
        assertEquals("Date", act.getDisplayName("startTime"));

        // verify that a node without a custom display name is an uncamel-cased
        // version of the node name
        IMObjectBean pet = createBean("animal.pet");
        assertEquals("Date Of Birth", pet.getDisplayName("dateOfBirth"));
    }

    /**
     * Tests the {@link IMObjectBean#getValue(String)} and
     * {@link IMObjectBean#setValue(String, Object)} for a non-existent node.
     */
    public void testGetSetInvalidNode() {
        IMObjectBean bean = createBean("party.customerperson");
        try {
            bean.getValue("badNode");
            assertTrue(false);
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }

        try {
            bean.setValue("badNode", "value");
            assertTrue(false);
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link IMObjectBean#getValue(String)} method.
     */
    public void testGetValue() {
        IMObjectBean bean = createBean("party.customerperson");
        assertEquals(bean.getValue("firstName"), null);
        bean.setValue("firstName", "Joe");
        assertEquals("Joe", bean.getValue("firstName"));
    }

    /**
     * Tests the {@link IMObjectBean#getBoolean(String)} method.
     */
    public void testGetBoolean() {
        IMObjectBean bean = createBean("animal.pet");
        assertEquals(bean.getBoolean("desexed"), false);
        bean.setValue("desexed", true);
        assertEquals(true, bean.getBoolean("desexed"));
    }

    /**
     * Tests the {@link IMObjectBean#getInt(String)} method.
     */
    public void testGetInt() {
        IMObjectBean bean = createBean("entityRelationship.animalOwner");
        assertEquals(0, bean.getInt("sequence"));
        bean.setValue("sequence", 1);
        assertEquals(1, bean.getInt("sequence"));
    }

    /**
     * Tests the {@link IMObjectBean#getLong(String)} method.
     */
    public void testGetLong() {
        IMObjectBean bean = createBean("document.common");
        long size = 10000000L;
        assertEquals(0, bean.getLong("size"));
        bean.setValue("size", size);
        assertEquals(size, bean.getLong("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getString(String)} method.
     */
    public void testGetString() {
        IMObjectBean bean = createBean("document.common");
        assertNull(bean.getValue("name"));
        bean.setValue("name", "invoice.pdf");
        assertEquals("invoice.pdf", bean.getValue("name"));

        // test conversion, long -> string
        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(Long.toString(size), bean.getString("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getBigDecimal(String)} method.
     */
    public void testGetBigDecimal() {
        IMObjectBean bean = createBean("act.customerAccountPayment");
        BigDecimal expected = new BigDecimal("1234.56");
        assertEquals(bean.getBigDecimal("amount"), new BigDecimal("0.0"));
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getBigDecimal("amount"));
    }

    /**
     * Tests the {@link IMObjectBean#getBigDecimal(String)} method.
     */
    public void testGetDate() {
        IMObjectBean bean = createBean("act.customerAccountPayment");
        Date expected = new Date();
        bean.setValue("startTime", expected);
        assertEquals(expected, bean.getDate("startTime"));
    }

    /**
     * Tests the {@link IMObjectBean#getValues(String)},
     * {@link IMObjectBean#addValue(String, IMObject)} and
     * {@link IMObjectBean#removeValue(String, IMObject)} methods.
     */
    public void testCollection() {
        IMObjectBean bean = createBean("party.customerperson");
        List<IMObject> values = bean.getValues("contacts");
        assertNotNull(values);
        assertEquals(0, values.size());
        IMObjectBean locationBean = createBean("contact.location");
        IMObjectBean phoneBean = createBean("contact.phoneNumber");

        IMObject location = locationBean.getObject();
        IMObject phone = phoneBean.getObject();
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        checkEquals(bean.getValues("contacts"), location, phone);

        bean.removeValue("contacts", location);
        checkEquals(bean.getValues("contacts"), phone);

        bean.removeValue("contacts", phone);
        assertEquals(0, bean.getValues("contacts").size());

        // removal of non-existent object is a no-op
        bean.removeValue("contacts", phone);
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
     * Helper to create an object and wrap it in an {@link IMObjectBean}.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <code>shortName</code>.
     */
    private IMObjectBean createBean(String shortName) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return new IMObjectBean(object);
    }

    /**
     * Verifies that two lists of objects match.
     */
    private void checkEquals(List<IMObject> actual,
                             IMObject ... expected) {
        assertEquals(actual.size(), expected.length);
        for (IMObject e : expected) {
            boolean found = false;
            for (IMObject a : actual) {
                if (e.equals(a)) {
                    found = true;
                    break;
                }
            }
            assertTrue("IMObject not found: " + e, found);
        }
    }

}
