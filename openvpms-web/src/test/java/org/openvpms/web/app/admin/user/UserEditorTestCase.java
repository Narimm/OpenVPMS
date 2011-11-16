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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.admin.user;

import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.security.User;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link UserEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class UserEditorTestCase extends AbstractAppTest {

    /**
     * Tests validation.
     */
    @Test
    public void testValidate() {
        User user = (User) create(UserArchetypes.USER);
        UserEditor editor = new UserEditor(user, null, new DefaultLayoutContext());
        assertFalse(editor.isValid());

        editor.setUsername("foo");
        assertFalse(editor.isValid());

        editor.setPassword("bar");
        assertFalse(editor.isValid());

        editor.setName("Foo");
        assertTrue(editor.isValid());

        editor.setPassword(null);
        assertFalse(editor.isValid());
    }
}
