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

package org.openvpms.web.component.im.doc;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;


/**
 * Tests the {@link DocumentActEditor} for templated documents that are generated on the fly i.e don't have a document
 * node, and don't support versioning.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class TemplatedDocumentActEditorTest extends AbstractDocumentActEditorTest {

    /**
     * Verifies that the template can be set, and that no documents are generated.
     */
    @Test
    public void testSetTemplate() {
        DocumentAct act = createAct();
        DocumentActEditor editor = createEditor(act);

        Entity template1 = createDocumentTemplate(act.getArchetypeId().getShortName());
        Entity template2 = createDocumentTemplate(act.getArchetypeId().getShortName());
        editor.setTemplate(template1);
        assertTrue(save(editor));

        assertNull(act.getDocument());

        editor.setTemplate(template2);
        assertTrue(save(editor));

        // now delete the act
        assertTrue(delete(editor));
        assertNull(get(act));
    }

}