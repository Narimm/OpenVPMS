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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.type;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.hl7.util.HL7Archetypes;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link InvestigationTypeEditor}.
 *
 * @author Tim Anderson
 */
public class InvestigationTypeEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that an {@link InvestigationTypeEditor} is returned by {@link IMObjectEditorFactory}.
     */
    @Test
    public void testEditorFactory() {
        Entity type = (Entity) create(InvestigationArchetypes.INVESTIGATION_TYPE);
        IMObjectEditorFactory factory = applicationContext.getBean(IMObjectEditorFactory.class);
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        IMObjectEditor editor = factory.create(type, null, context);
        assertTrue(editor instanceof InvestigationTypeEditor);
    }

    /**
     * Verifies that both <em>universalServiceIdentifier</em> and </em><em>laboratory</em> are required if one is
     * specified.
     */
    @Test
    public void testLaboratoryValidation() {
        Entity laboratory = (Entity) TestHelper.create(HL7Archetypes.LABORATORY);
        User user = TestHelper.createUser();
        Party location = TestHelper.createLocation();
        laboratory.setName("ZLaboratory");
        IMObjectBean bean = new IMObjectBean(laboratory);
        bean.addNodeTarget("user", user);
        bean.addNodeTarget("location", location);
        bean.save();

        Entity type = (Entity) create(InvestigationArchetypes.INVESTIGATION_TYPE);
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        InvestigationTypeEditor editor = new InvestigationTypeEditor(type, null, context);
        editor.getComponent();
        assertFalse(editor.isValid());

        editor.getProperty("name").setValue("ZInvestigationType");
        assertTrue(editor.isValid());

        editor.setUniversalServiceIdentifier("Bloods");
        assertFalse(editor.isValid());

        editor.setLaboratory(laboratory);
        assertTrue(editor.isValid());

        editor.setLaboratory(null);
        assertFalse(editor.isValid());

        editor.setUniversalServiceIdentifier(null);
        assertTrue(editor.isValid());
    }
}
