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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;
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
     * Tests the {@link IMObjectBean#isA} method.
     */
    public void testIsA() {
        IMObjectBean bean = createBean("act.customerAccountPayment");
        String[] matches = {"act.customerAccountPaymentCash",
                            "act.customerAccountPayment"};
        assertTrue(bean.isA(matches));

        String[] nomatches = {"act.customerAccountPaymentCash",
                              "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(bean.isA(nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(bean.isA(wildmatch));

        String[] wildnomatch = {"act.customerEstimation*",
                                "act.customerInvoice*"};
        assertFalse(bean.isA(wildnomatch));

    }

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
        IMObjectBean pet = createBean("party.animalpet");
        assertEquals("Patient(Pet)", pet.getDisplayName());

        // verify shortname is returned when no display name is present
        IMObjectBean customer = createBean("party.customerperson");
        assertEquals("Customer(Person)", customer.getDisplayName());
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName(String)} method.
     */
    public void testNodeDisplayName() {
        IMObjectBean act = createBean("act.customerAccountPayment");
        assertEquals("Date", act.getDisplayName("startTime"));

        // verify that a node without a custom display name is an uncamel-cased
        // version of the node name
        IMObjectBean pet = createBean("party.animalpet");
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
        IMObjectBean bean = createBean("act.types");
        assertEquals(false, bean.getBoolean("flag"));
        assertEquals(true, bean.getBoolean("flag", true));

        bean.setValue("flag", true);
        assertEquals(true, bean.getBoolean("flag"));
    }

    /**
     * Tests the {@link IMObjectBean#getInt} methods.
     */
    public void testGetInt() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(0, bean.getInt("size"));
        assertEquals(-1, bean.getInt("size", -1));

        int size = 100;
        bean.setValue("size", size);
        assertEquals(size, bean.getInt("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getLong} methods.
     */
    public void testGetLong() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(0, bean.getLong("size"));
        assertEquals(-1, bean.getLong("size", -1));

        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(size, bean.getLong("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getString} methods.
     */
    public void testGetString() {
        IMObjectBean bean = createBean("act.types");
        assertNull(bean.getValue("name"));
        assertEquals("foo", bean.getString("name", "foo"));

        bean.setValue("name", "bar");
        assertEquals("bar", bean.getValue("name"));

        // test conversion, long -> string
        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(Long.toString(size), bean.getString("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getBigDecimal} methods.
     */
    public void testGetBigDecimal() {
        IMObjectBean bean = createBean("act.types");

        assertNull(bean.getBigDecimal("amount"));
        assertEquals(bean.getBigDecimal("amount", BigDecimal.ZERO),
                     BigDecimal.ZERO);

        BigDecimal expected = new BigDecimal("1234.56");
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getBigDecimal("amount"));

        // quantity has a default value
        assertEquals(BigDecimal.ONE, bean.getBigDecimal("quantity"));
    }

    /**
     * Tests the {@link IMObjectBean#getMoney} methods.
     */
    public void testMoney() {
        IMObjectBean bean = createBean("act.types");

        assertNull(bean.getMoney("amount"));
        assertEquals(bean.getMoney("amount", new Money(0)), new Money(0));

        Money expected = new Money("1234.56");
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getMoney("amount"));
    }

    /**
     * Tests the {@link IMObjectBean#getDate} methods.
     */
    public void testGetDate() {
        IMObjectBean bean = createBean("act.types");

        Date now = new Date();
        assertNull(bean.getDate("endTime"));
        assertEquals(bean.getDate("endTime", now), now);

        bean.setValue("endTime", now);
        assertEquals(now, bean.getDate("endTime"));
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
     * Tests the {@link IMObjectBean#getValues(String, Class)} method.
     */
    public void testGetValuesTypeSafeCast() {
        IMObjectBean bean = createBean("party.customerperson");
        List<IMObject> values = bean.getValues("contacts");
        assertNotNull(values);
        assertEquals(0, values.size());
        IMObjectBean locationBean = createBean("contact.location");
        IMObjectBean phoneBean = createBean("contact.phoneNumber");

        Contact location = (Contact) locationBean.getObject();
        Contact phone = (Contact) phoneBean.getObject();
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        List<Contact> contacts = bean.getValues("contacts", Contact.class);
        checkEquals(contacts, location, phone);

        try {
            bean.getValues("contacts", Act.class);
            fail("Expected IMObjectBeanException");
        } catch (IMObjectBeanException exception) {
            assertEquals(InvalidClassCast, exception.getErrorCode());
            assertEquals("Expected class of type " + Act.class.getName()
                    + " but got " + Contact.class.getName(),
                         exception.getMessage());
        }
    }

    /**
     * Tests {@link IMObjectBean#save}.
     */
    public void testSave() {
        IMObjectBean bean = createBean("act.types");
        IMObject object = bean.getObject();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String name = getClass().getName() + bean.hashCode();
        bean.setValue("name", name);
        bean.save();
        object = ArchetypeQueryHelper.getByObjectReference(
                service, object.getObjectReference());
        bean = new IMObjectBean(object);
        assertEquals(name, bean.getValue("name"));
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
    private <T extends IMObject> void checkEquals(List<T> actual,
                                                  T ... expected) {
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
