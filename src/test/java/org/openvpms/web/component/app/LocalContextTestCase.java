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

package org.openvpms.web.component.app;

import org.apache.commons.beanutils.MethodUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.test.AbstractAppTest;

import java.lang.reflect.Method;
import java.util.Date;


/**
 * Tests the {@link LocalContext} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LocalContextTestCase extends AbstractAppTest {

    /**
     * Verfies that values specified in a local context overrides those
     * specified in the parent.
     *
     * @throws Exception for any error
     */
    public void testContext() throws Exception {
        LocalContext context = createContext(true);
        checkContext("security.user", "getClinician", context);
        checkContext("act.customerAppointment", "getCurrent", context);
        checkContext("party.customerperson", "getCustomer", context);
        checkContext("party.organisationDeposit", "getDeposit", context);
        checkContext("party.organisationLocation", "getLocation", context);
        checkContext("party.patientpet", "getPatient", context);
        checkContext("party.organisationPractice", "getPractice", context);
        checkContext("product.medication", "getProduct", context);
        checkContext("entity.scheduleViewType", "getScheduleViewType", context);
        checkContext("party.organisationSchedule", "getSchedule", context);
        checkContext(new Date(), new Date(), "getScheduleDate", context);
        checkContext("party.organisationStockLocation", "getStockLocation",
                     context);
        checkContext("party.supplierperson", "getSupplier", context);
        checkContext("party.organisationTill", "getTill", context);
        checkContext("security.user", "getUser", context);
        checkContext("party.organisationWorkList", "getWorkList", context);
        checkContext(new Date(), new Date(), "getWorkListDate", context);
    }

    /**
     * Verifies that {@link LocalContext#removeObject} doesn't remove objects
     * from the parent.
     */
    public void testRemoveObject() {
        LocalContext context = createContext(true);
        Context parent = context.getParent();

        User user1 = (User) create("security.user");
        User user2 = (User) create("security.user");
        context.setUser(user1);
        parent.setUser(user2);

        context.removeObject(user1);
        assertEquals(user2, context.getUser()); // should inherit parent

        context.removeObject(user2);            // should do nothing
        assertEquals(user2, parent.getUser());
        assertEquals(user2, context.getUser()); // should inherit parent
    }

    /**
     * Verfies that values specified in a local context overrides those
     * specified in the parent.
     *
     * @param shortName the archetype short name of the value to create
     * @param method    the context getter method name
     * @param context   the context
     * @throws Exception for any error
     */
    private void checkContext(String shortName, String method,
                              LocalContext context) throws Exception {
        Object object1 = create(shortName);
        Object object2 = create(shortName);
        checkContext(object1, object2, method, context);
    }

    /**
     * Verfies that values specified in a local context overrides those
     * specified in the parent.
     *
     * @param obj1    the first object
     * @param obj2    the second object
     * @param method  the context getter method name
     * @param context the context
     * @throws Exception for any error
     */
    private void checkContext(Object obj1, Object obj2, String method,
                              LocalContext context) throws Exception {
        assertNull(get(context, method));
        String setter = method.replaceFirst("get", "set");
        Context parent = context.getParent();

        if (parent != null) {
            set(parent, setter, obj1);
            assertEquals(obj1, get(context, method));
        }

        set(context, setter, obj2);
        assertEquals(obj2, get(context, method));

        if (parent != null) {
            assertEquals(obj1, get(parent, method));
            set(parent, setter, null);
            assertEquals(obj2, get(context, method));
        }

        set(context, setter, null);
        assertNull(get(context, method));
    }

    /**
     * Creates a new local context.
     *
     * @param withParent if <tt>true</tt> soecify a parent
     * @return a new context
     */
    private LocalContext createContext(boolean withParent) {
        Context parent = null;
        if (withParent) {
            parent = new AbstractContext() {
            };
        }
        return new LocalContext(parent);
    }

    /**
     * Invokes the named context setter method.
     *
     * @param context    the context
     * @param methodName the method name
     * @param value      the value to set
     * @throws Exception for any error
     */
    private void set(Context context, String methodName, Object value)
            throws Exception {
        Method[] methods = context.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                method.invoke(context, value);
                break;
            }
        }
    }

    /**
     * Invokes the named context getter method.
     *
     * @param context the context
     * @param method  the method name
     * @return the method value
     * @throws Exception for any error
     */
    private Object get(Context context, String method) throws Exception {
        return MethodUtils.invokeMethod(context, method, null);
    }

    /**
     * Helper to create an instance of the specified archetype.
     *
     * @param shortName the archetype short name
     * @return a new object
     */
    private IMObject create(String shortName) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }
}
