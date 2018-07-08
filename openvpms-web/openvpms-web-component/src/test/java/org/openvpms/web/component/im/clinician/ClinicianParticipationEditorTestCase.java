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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.clinician;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ClinicianParticipationEditor}.
 *
 * @author Tim Anderson
 */
public class ClinicianParticipationEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that the clinician is populated from the context for new participation instances, unless the
     * clinician is inactive.
     */
    @Test
    public void testNewParticipation() {
        User clinician = TestHelper.createClinician();
        Act event = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        Context context = new LocalContext();
        context.setClinician(clinician);
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));

        // create a new participation, and verify it inherits the clinician from the context
        Participation participation1 = (Participation) create(UserArchetypes.CLINICIAN_PARTICIPATION);
        ClinicianParticipationEditor editor1 = new ClinicianParticipationEditor(participation1, event, layoutContext);

        assertEquals(clinician, editor1.getEntity());
        assertTrue(editor1.isValid());

        // now inactivate the clinician. The editor should still be valid
        clinician.setActive(false);
        save(clinician);
        assertTrue(editor1.isValid());

        // create a new participation, and verify the clinician isn't populated as it is now inactive
        Participation participation2 = (Participation) create(UserArchetypes.CLINICIAN_PARTICIPATION);
        ClinicianParticipationEditor editor2 = new ClinicianParticipationEditor(participation2, event, layoutContext);
        assertNull(editor2.getEntity());
        assertFalse(editor2.isValid());

        // populate it explicitly
        editor2.setEntity(clinician);
        assertEquals(clinician, editor2.getEntity());
        assertTrue(editor2.isValid());
    }

    /**
     * Verify that the context clinician doesn't override an existing clinician.
     */
    @Test
    public void testExistingParticipation() {
        User clinician1 = TestHelper.createClinician();
        User clinician2 = TestHelper.createClinician();
        Act event = PatientTestHelper.createEvent(TestHelper.createPatient(), clinician1);

        Context context = new LocalContext();
        context.setClinician(clinician2);
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));

        Participation participation = (Participation) getArchetypeService().getBean(event).getObject("clinician");
        assertNotNull(participation);

        ClinicianParticipationEditor editor = new ClinicianParticipationEditor(participation, event, layoutContext);
        assertEquals(clinician1, editor.getEntity());
    }
}
