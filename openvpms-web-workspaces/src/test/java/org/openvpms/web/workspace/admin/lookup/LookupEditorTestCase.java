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

package org.openvpms.web.workspace.admin.lookup;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link LookupEditor}.
 *
 * @author Tim Anderson
 */
public class LookupEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that the editor is marked invalid if a lookup has a code that is the same as an existing lookup.
     */
    @Test
    public void testDuplicate() {
        String duplicate = "CBA";
        TestHelper.getLookup("lookup.bank", duplicate); // make sure the lookup exists

        Lookup lookup = (Lookup) create("lookup.bank"); // create a new lookup, and edit it
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(lookup, context);
        assertTrue(editor instanceof LookupEditor);
        Property name = editor.getProperty("name");
        assertNotNull(name);
        name.setValue("Foo Bank");
        assertEquals("FOO_BANK", lookup.getCode());     // verify the code is updated
        assertTrue(editor.isValid());                   // editor should be valid

        name.setValue(duplicate);                       // now change the lookup to have the same code as an above
        assertEquals("CBA", lookup.getCode());          // verify the code is updated
        assertFalse(editor.isValid());                  // editor should now be invalid

        Validator validator = new DefaultValidator();          // ensure the validation error is that expected
        assertFalse(editor.validate(validator));
        List<ValidatorError> errors = validator.getErrors(editor);
        assertEquals(1, errors.size());
        ValidatorError error = errors.get(0);
        String editorDisplay = editor.getDisplayName();
        String codeDisplay = editor.getProperty("code").getDisplayName();
        String message = Messages.format("lookup.validation.duplicate", editorDisplay, codeDisplay, duplicate);
        String expected = Messages.format(ValidatorError.NODE_KEY, editorDisplay, codeDisplay, message);
        assertEquals(expected, error.toString());
    }


}
