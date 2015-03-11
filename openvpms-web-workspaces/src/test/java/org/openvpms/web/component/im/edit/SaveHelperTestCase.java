/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link SaveHelper} class.
 *
 * @author Tim Anderson
 */
public class SaveHelperTestCase extends AbstractAppTest {

    /**
     * Errors logged by the error handler.
     */
    private List<String> errors;


    /**
     * Tests the {@link SaveHelper#save(IMObjectEditor)} method.
     */
    @Test
    public void testSaveEditor() {
        // create a valid customer, and set its lastName node to null to invalidate it
        IMObject customer = TestHelper.createCustomer(false);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("lastName", null);

        // create an editor for the customer
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(customer, context);

        // verify save fails. The id should by -1
        assertFalse(SaveHelper.save(editor));
        assertEquals(-1, customer.getId());

        // verify the ErrorHandler was called
        String lastName = bean.getDisplayName("lastName");
        String expected = Messages.format("org.openvpms.component.business.service.archetype.ValidationError.formatted",
                                          bean.getDisplayName(), lastName,
                                          Messages.format("property.error.required", lastName));
        assertEquals(1, errors.size());
        assertEquals(expected, errors.get(0));

        errors.clear();

        // now make the customer valid, and verify it saves
        editor.getProperty("lastName").setValue("ZFoo");
        assertTrue(SaveHelper.save(editor));
        assertEquals(0, errors.size());

        // should have an id now
        assertFalse(customer.getId() == -1);

        // delete the object, and verify a subsequent save fails
        remove(customer);
        editor.getProperty("lastName").setValue("ZFoo2"); // need to modify it in order for save to proceed
        assertFalse(SaveHelper.save(editor));
        expected = Messages.format("imobject.notfound", bean.getDisplayName());
        assertEquals(2, errors.size());
        assertEquals(expected, errors.get(0)); // Only care about the first exception. Second is a rollback exception
    }

    /**
     * Tests the {@link SaveHelper#save(IMObject)} method.
     */
    @Test
    public void testSaveObject() {
        // create a valid customer, and set its lastName node to null to invalidate it
        IMObject customer = TestHelper.createCustomer(false);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("lastName", null);

        // verify save fails. The id should by -1
        assertFalse(SaveHelper.save(customer));
        assertEquals(-1, customer.getId());

        // verify the ErrorHandler was called
        String lastName = bean.getDisplayName("lastName");
        String expected = Messages.format("org.openvpms.component.business.service.archetype.ValidationError.formatted",
                                          bean.getDisplayName(), lastName, "value is required");
        assertEquals(1, errors.size());
        assertEquals(expected, errors.get(0));

        // now make the customer valid, and verify it saves
        bean.setValue("lastName", "ZFoo");
        errors.clear();
        assertTrue(SaveHelper.save(customer));
        assertEquals(0, errors.size());

        // should have an id now
        assertFalse(customer.getId() == -1);

        // delete the object, and verify a subsequent save fails
        remove(customer);
        assertFalse(SaveHelper.save(customer));
        expected = Messages.format("imobject.notfound", bean.getDisplayName());
        assertEquals(1, errors.size());
        assertEquals(expected, errors.get(0));
    }

    /**
     * Tests the {@link SaveHelper#delete(IMObject)} method.
     */
    @Test
    public void testDelete() {
        // create a valid customer
        IMObject object = TestHelper.createCustomer(false);

        // NOTE: delete succeeds despite not being saved
        assertTrue(SaveHelper.delete(object));

        // save object, and verify it can be deleted
        save(object);
        assertTrue(SaveHelper.delete(object));
        assertEquals(0, errors.size());

        // verify a subsequent delete fails, and that the ErrorHandler was invoked
        assertFalse(SaveHelper.delete(object));

        IMObjectBean bean = new IMObjectBean(object);
        String expected = Messages.format("imobject.notfound", bean.getDisplayName());
        assertEquals(1, errors.size());
        assertEquals(expected, errors.get(0));
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        errors = new ArrayList<String>();

        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
            }
        });
    }
}
