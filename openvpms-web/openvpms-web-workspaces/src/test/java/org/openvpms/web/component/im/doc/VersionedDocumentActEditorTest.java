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

import org.junit.Test;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link DocumentActEditor} for versioned documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class VersionedDocumentActEditorTest extends AbstractDocumentActEditorTest {

    /**
     * Verifies that the earlier version of a document is saved if its content is updated and the archetype supports
     * versioning.
     */
    @Test
    public void testVersioning() {
        DocumentAct act = createAct();
        DocumentActEditor editor = createEditor(act);
        Document doc1 = createDocument();
        Document doc2 = createDocument();
        Document doc3 = createDocument();

        editor.setDocument(doc1);
        assertEquals(doc1.getObjectReference(), act.getDocument());
        save(editor);

        editor.setDocument(doc2);
        assertEquals(doc2.getObjectReference(), act.getDocument());
        save(editor);

        editor.setDocument(doc3);
        assertEquals(doc3.getObjectReference(), act.getDocument());
        save(editor);

        ActBean bean = new ActBean(act);
        List<DocumentAct> versions = sort(bean.getNodeActs("versions", DocumentAct.class));
        assertEquals(2, versions.size());
        DocumentAct version1 = versions.get(0);
        assertEquals(doc1.getObjectReference(), version1.getDocument());

        DocumentAct version2 = versions.get(1);
        assertEquals(doc2.getObjectReference(), version2.getDocument());

        // make sure all the documents are accessible
        assertNotNull(get(doc1));
        assertNotNull(get(doc2));
        assertNotNull(get(doc3));

        // now delete the act, and verify everything is deleted
        delete(editor);
        assertNull(get(act));
        assertNull(get(version1));
        assertNull(get(version2));
        assertNull(get(doc1));
        assertNull(get(doc2));
        assertNull(get(doc3));
    }

    /**
     * Creates a new document.
     *
     * @return a new document
     */
    protected abstract Document createDocument();

    /**
     * Sorts document versions based on their timestamp and id.
     *
     * @param versions the versions
     * @return the sorted versions
     */
    protected List<DocumentAct> sort(List<DocumentAct> versions) {
        Collections.sort(versions, new Comparator<DocumentAct>() {
            public int compare(DocumentAct o1, DocumentAct o2) {
                int result = o1.getActivityStartTime().compareTo(o2.getActivityStartTime());
                return (result == 0) ? (int) (o1.getId() - o2.getId()) : result;
            }
        });
        return versions;
    }
}
