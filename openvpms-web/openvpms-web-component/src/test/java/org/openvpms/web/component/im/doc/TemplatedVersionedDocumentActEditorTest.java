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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.junit.Test;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DocumentActEditor} class, for templated, versioned archetypes.
 *
 * @author Tim Anderson
 */
public abstract class TemplatedVersionedDocumentActEditorTest extends VersionedDocumentActEditorTest {

    /**
     * Verifies that a document is generated when a template is associated with an <em>act.patientDocumentLetter</em>,
     * and that this is deleted when the act is deleted.
     */
    @Test
    public void testDocGenerationFromTemplate() {
        DocumentAct act = createAct();
        Entity template = createDocumentTemplate(act.getArchetypeId().getShortName());
        DocumentActEditor editor = createEditor(act);
        editor.getComponent();

        editor.setTemplate(template);
        IMObjectReference docRef = act.getDocument();
        assertNotNull(docRef);
        assertTrue(save(editor));

        // verify the document was generated and saved
        Document doc = (Document) get(docRef);
        assertNotNull(doc);
        assertEquals(DocFormats.PDF_TYPE, doc.getMimeType());
        assertEquals("blank.pdf", doc.getName());

        // now delete the act and verify both it and the document were deleted
        boolean result = delete(editor);
        assertTrue(result);
        assertNull(get(act));
        assertNull(get(docRef));
    }

    /**
     * Verifies that:
     * <ol>
     * <li> a document is generated when a template is associated with an <em>act.patientDocumentLetter</em>; and
     * <li>subsequent setting a new template generates a new document, versioning the original one; and
     * <li>all are deleted when the parent is deleted
     * </ol>
     */
    @Test
    public void testGenerationWithVersioning() {
        DocumentAct act = createAct();
        Entity template1 = createDocumentTemplate(act.getArchetypeId().getShortName());
        Entity template2 = createDocumentTemplate(act.getArchetypeId().getShortName());
        DocumentActEditor editor = createEditor(act);
        editor.getComponent();

        editor.setTemplate(template1);
        IMObjectReference docRef1 = act.getDocument();
        assertNotNull(docRef1);
        assertTrue(save(editor));

        editor.setTemplate(template2);
        IMObjectReference docRef2 = act.getDocument();
        assertNotNull(docRef2);
        assertTrue(save(editor));

        ActBean bean = new ActBean(act);
        List<DocumentAct> versions = bean.getNodeActs("versions", DocumentAct.class);
        assertEquals(1, versions.size());
        DocumentAct version1 = versions.get(0);
        assertEquals(docRef1, version1.getDocument());

        // now delete the act and verify both it and the document were deleted
        boolean result = delete(editor);
        assertTrue(result);
        assertNull(get(act));
        assertNull(get(version1));
        assertNull(get(docRef1));
        assertNull(get(docRef2));
    }

}
