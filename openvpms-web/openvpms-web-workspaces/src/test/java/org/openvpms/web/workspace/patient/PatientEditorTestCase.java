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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PatientEditor}.
 *
 * @author Tim Anderson
 */
public class PatientEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link PatientEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        Party patient = TestHelper.createPatient();
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        PatientEditor editor = new PatientEditor(patient, null, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof PatientEditor);
    }
}
