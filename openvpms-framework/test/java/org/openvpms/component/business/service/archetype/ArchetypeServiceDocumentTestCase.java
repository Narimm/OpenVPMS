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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.springframework.test.context.ContextConfiguration;


/**
 * Test the ability to create and query on Documentss.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServiceDocumentTestCase extends AbstractArchetypeServiceTest {


    /**
     * Test the creation of a Document using the {@link NodeDescriptor}s.
     */
    @Test
    public void testDocumentCreationThruNodeDescriptors() {
        Document document = (Document) create("document.common");
        ArchetypeDescriptor adesc = getArchetypeDescriptor("document.common");
        NodeDescriptor ndesc;

        // set the name
        ndesc = adesc.getNodeDescriptor("name");
        assertNotNull(ndesc);
        ndesc.setValue(document, "name.doc");
        assertTrue(document.getName().equals("name.doc"));

        // set the mime type
        ndesc = adesc.getNodeDescriptor("mimeType");
        assertNotNull(ndesc);
        ndesc.setValue(document, "test/palin");
        assertTrue(document.getMimeType().equals("test/palin"));

        // set the doc size
        ndesc = adesc.getNodeDescriptor("size");
        assertNotNull(ndesc);
        ndesc.setValue(document, "1000");
        assertTrue(document.getDocSize() == 1000);

        // set the checksum
        ndesc = adesc.getNodeDescriptor("checksum");
        assertNotNull(ndesc);
        ndesc.setValue(document, "1234");
        assertTrue(document.getChecksum() == 1234);

        // set the contents
        ndesc = adesc.getNodeDescriptor("contents");
        assertNotNull(ndesc);
        ndesc.setValue(document, "Jim Alateras".getBytes());
        assertTrue(document.getContents().length == "Jim Alateras".length());

        // save the document
        save(document);
    }

    /**
     * Test the creation of a simple document
     */
    @Test
    public void testSimpleDocumentCreation() {
        Document document = createDocument("jima.doc", "text/plain", 12, 12343,
                                           "Jim Alateras".getBytes());
        save(document);
    }

    /**
     * Test the creation of multiple document and the retrieval of some documents
     */
    @Test
    public void testMultipleDocumentCreation() {
        Document doc;
        IMObjectReference ref = null;

        for (int index = 0; index < 10; index++) {
            doc = createDocument("jima" + index + ".doc",
                                 "text/plain", 12 + index, 12343 + index,
                                 ("Jim Alateras" + index).getBytes());
            save(doc);
            ref = doc.getObjectReference();
        }

        // retrieve the last document
        assertNotNull(get(ref));
    }

    /**
     * Test the creation of a simple document act.
     */
    @Test
    public void testDocumentActCreation() {
        Document document = createDocument("jima.doc", "text/plain", 12, 12343, "Jim Alateras".getBytes());
        save(document);
        DocumentAct docAct = createDocumentAct(document);
        save(docAct);
    }

    /**
     * Test creation and retrieval of a document act.
     */
    @Test
    public void testDocumentActRetrieval() {
        Document document = createDocument("jima.doc", "text/plain", 234, 12343, "Jim Alateras".getBytes());
        save(document);
        DocumentAct docAct = createDocumentAct(document);
        save(docAct);

        // retrieve it again
        assertNotNull(get(docAct));
    }

    /**
     * Test the modification of a document act.
     */
    @Test
    public void testDocumentActModification() {
        Document document = createDocument("jima.doc", "text/plain", 234, 12343, "Jim Alateras".getBytes());
        save(document);
        DocumentAct docAct = createDocumentAct(document);
        save(docAct);

        // reload the act
        docAct = get(docAct);
        assertEquals(document.getObjectReference(), docAct.getDocument());

        // create a new document reference
        document = createDocument("tima.doc", "text/plain", 234, 12343, "Tim Anderson".getBytes());
        save(document);
        docAct.setDocument(document.getObjectReference());
        save(docAct);

        // retrieve and check again
        docAct = get(docAct);
        assertEquals(document.getObjectReference(), docAct.getDocument());
    }

    /**
     * Test the deletion of a document act.
     */
    @Test
    public void testDocumentActDeletion() {
        Document document = createDocument("jima.doc", "text/plain", 234, 12343, "Jim Alateras".getBytes());
        save(document);
        DocumentAct docAct = createDocumentAct(document);
        save(docAct);

        // delete the document act
        remove(docAct);

        // ensure that we can still retrieve the document
        assertNotNull(get(document));

        // check that we can't retrieve the document act
        assertNull(get(docAct));
    }

    /**
     * Create a document with the specified parameters
     *
     * @param name     the document name
     * @param mime     the mime type
     * @param size     the size of the doc
     * @param checksum the associated checksum
     * @param contents the contents
     * @return Document
     */
    public Document createDocument(String name, String mime, int size, long checksum, byte[] contents) {
        Document document = (Document) create("document.common");
        document.setName(name);
        document.setMimeType(mime);
        document.setDocSize(size);
        document.setChecksum(checksum);
        document.setContents(contents);

        return document;
    }

    /**
     * Create a document act given the specified document
     *
     * @param document the document to associate with the act
     * @return DocumentAct
     */
    public DocumentAct createDocumentAct(Document document) {
        DocumentAct act = (DocumentAct) create("document.act");
        act.setName("documentAct1");
        act.setDescription("This is the first document act");
        act.setDocVersion("ver1");
        act.setDocument(document.getObjectReference());

        return act;
    }

}
