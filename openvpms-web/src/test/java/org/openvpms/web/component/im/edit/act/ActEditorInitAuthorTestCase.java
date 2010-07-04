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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.edit.act;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.util.List;


/**
 * Verifies that for each act that has an "author" node, the corresponding editor populates it with the current user,
 * as per OVPMS-559.
 * <p/>
 * TODO - perhaps author information should be populated on save by a rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActEditorInitAuthorTestCase extends AbstractAppTest {

    /**
     * Verifies that for each act archetype with an "author" node, the node is populated when the act is edited.
     */
    @Test
    public void testInitAuthor() {
        // set up the context
        IArchetypeService service = ServiceHelper.getArchetypeService();
        GlobalContext context = GlobalContext.getInstance();
        User user = TestHelper.createUser();
        context.setUser(user);

        int count = 0;

        // find all act archetypes
        List<ArchetypeDescriptor> archetypes = service.getArchetypeDescriptors("act.*");
        for (ArchetypeDescriptor archetype : archetypes) {
            if (archetype.getNodeDescriptor("author") != null) {
                // found an archetype with an author node
                ++count;

                // create an instance
                String shortName = archetype.getType().getShortName();
                IMObject object = service.create(shortName);
                assertNotNull(object);
                assertTrue(object instanceof Act);

                // create an editor for the act
                IMObjectEditor editor = IMObjectEditorFactory.create(object, new DefaultLayoutContext());
                ActBean bean = new ActBean((Act) editor.getObject(), service);

                // verify the author node has been populated
                assertEquals(user.getObjectReference(), bean.getNodeParticipantRef("author"));
            }
        }
        assertFalse(count == 0);
    }

}
