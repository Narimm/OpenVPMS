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
 */

package org.openvpms.web.component.im.edit.act;


import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link SingleParticipationCollectionEditor} class.
 *
 * @author Tim Anderson
 */
public class SingleParticipationCollectionEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that optional participations are excluded when there is no associated entity.
     */
    @Test
    public void testOptionalParticipation() {
        User clinician = TestHelper.createClinician();
        Act act = (Act) create(CustomerAccountArchetypes.INVOICE_ITEM);
        ActBean bean = new ActBean(act);
        assertNull(bean.getParticipation(UserArchetypes.CLINICIAN_PARTICIPATION));

        // create a SingleParticipationCollectionEditor for a clinician participation
        CollectionProperty property = createCollectionProperty(act, "clinician");
        SingleParticipationCollectionEditor editor = new SingleParticipationCollectionEditor(property, act,
                new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null)));
        editor.getComponent();
        assertTrue(editor.isEmpty());
        assertTrue(editor.isValid());
        assertNull(bean.getParticipation(UserArchetypes.CLINICIAN_PARTICIPATION));

        // set a valid clinician. The participation will be added to the act
        ClinicianParticipationEditor clinicianEditor = (ClinicianParticipationEditor) editor.getCurrentEditor();
        clinicianEditor.setEntity(clinician);
        assertFalse(editor.isEmpty());
        assertTrue(editor.isValid());
        assertNotNull(bean.getParticipation(UserArchetypes.CLINICIAN_PARTICIPATION));

        // remove the clinician. The participation will be removed from the act
        clinicianEditor.setEntity(null);
        assertTrue(editor.isEmpty());
        assertTrue(editor.isValid());
        assertNull(bean.getParticipation(UserArchetypes.CLINICIAN_PARTICIPATION));
    }

    /**
     * Creates a new collection property.
     *
     * @param act  the parent act
     * @param name the node name
     * @return a new collection property
     */
    private CollectionProperty createCollectionProperty(Act act, String name) {
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(act);
        assertNotNull(archetype);
        NodeDescriptor node = archetype.getNodeDescriptor(name);
        assertNotNull(node);
        return new IMObjectProperty(act, node);

    }
}
