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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DocumentTemplateEditor}.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateEditorTestCase extends AbstractAppTest {

    /**
     * Verifies a document can be associated with a template.
     */
    @Test
    public void testUpload() {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplateEditor editor = createEditor(template);
        editor.getProperty("name").setValue("Z Test template");
        Document document1 = DocumentTestHelper.createDocument("/blank.jrxml");
        editor.onUpload(document1);
        assertTrue(SaveHelper.save(editor));
        Document original = checkDocument(template, document1);

        // now replace the document with another
        Document document2 = DocumentTestHelper.createDocument("/sqlreport.jrxml");
        editor.onUpload(document2);
        assertTrue(SaveHelper.save(editor));
        checkDocument(template, document2);
        assertNull(get(original));            // should have been deleted
    }

    /**
     * Verifies that when multiple uploads are done prior to save, only the last instance of a document is kept.
     */
    @Test
    public void testUploadTwice() {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplateEditor editor = createEditor(template);
        editor.getProperty("name").setValue("Z Test template");
        Document document1 = DocumentTestHelper.createDocument("/blank.jrxml");
        editor.onUpload(document1);

        // verify the document has been saved
        document1 = get(document1);
        assertNotNull(document1);

        // upload a new document
        Document document2 = DocumentTestHelper.createDocument("/sqlreport.jrxml");
        editor.onUpload(document2);
        assertNotNull(get(document1));       // document1 should still exist

        assertTrue(SaveHelper.save(editor));
        checkDocument(template, document2);

        assertNull(get(document1));          // document1 should have been deleted
    }

    /**
     * Verifies a document can be replaced by another editor instance.
     */
    @Test
    public void testReplaceDocument() {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplateEditor editor1 = createEditor(template);

        editor1.getProperty("name").setValue("Z Test template");
        Document document1 = DocumentTestHelper.createDocument("/blank.jrxml");
        editor1.onUpload(document1);
        assertTrue(SaveHelper.save(editor1));
        Document actual1 = checkDocument(template, document1);

        // now replace the document with another editor
        DocumentTemplateEditor editor2 = createEditor(template);
        Document document2 = DocumentTestHelper.createDocument("/sqlreport.jrxml");
        editor2.onUpload(document2);
        assertTrue(SaveHelper.save(editor2));
        Document actual2 = checkDocument(template, document2);
        assertNull(get(actual1));        // should have been deleted

        // and again
        DocumentTemplateEditor editor3 = createEditor(template);
        Document document3 = DocumentTestHelper.createDocument("/blank.jrxml");
        editor3.onUpload(document3);
        assertTrue(SaveHelper.save(editor3));
        checkDocument(template, document3);
        assertNull(get(actual2));        // should have been deleted
    }

    /**
     * Verifies that when a template is deleted, the associated document act and document is also deleted.
     */
    @Test
    public void testDelete() {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplateEditor editor1 = createEditor(template);

        editor1.getProperty("name").setValue("Z Test template");
        Document document = DocumentTestHelper.createDocument("/blank.jrxml");
        editor1.onUpload(document);

        DocumentAct act = editor1.getDocumentAct();
        assertNotNull(act);
        assertEquals(document.getObjectReference(), act.getDocument());

        assertTrue(SaveHelper.save(editor1));
        assertNotNull(get(act));
        assertNotNull(get(document));

        final DocumentTemplateEditor editor2 = createEditor(template);
        TransactionTemplate txn = new TransactionTemplate(ServiceHelper.getTransactionManager());
        txn.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                editor2.delete();
            }
        });

        // verify they have been deleted
        assertNull(get(template));
        assertNull(get(act));
        assertNull(get(document));
    }

    /**
     * Helper to create an editor to edit a template.
     *
     * @param template the template to edit
     * @return a new editor
     */
    protected DocumentTemplateEditor createEditor(Entity template) {
        DefaultLayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        DocumentTemplateEditor editor = new DocumentTemplateEditor(template, null, context);
        editor.getComponent();
        return editor;
    }

    /**
     * Verifies the document associated with an <em>entity.documentTemplate</em> matches that expected.
     *
     * @param template the template
     * @param expected the expected document
     * @return the actual document
     */
    private Document checkDocument(Entity template, Document expected) {
        TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
        DocumentAct act = helper.getDocumentAct(template);
        assertNotNull(act);
        assertEquals(expected.getName(), act.getFileName());
        assertEquals(expected.getMimeType(), act.getMimeType());
        Document document = (Document) get(act.getDocument());
        assertNotNull(document);
        assertEquals(expected.getName(), document.getName());
        assertEquals(expected.getMimeType(), document.getMimeType());
        assertEquals(expected.getDocSize(), document.getDocSize());
        assertEquals(expected.getChecksum(), document.getChecksum());
        return document;
    }
}
